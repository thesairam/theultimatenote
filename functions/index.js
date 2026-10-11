const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");

const groqApiKey = defineSecret("GROQ_API_KEY");
const geminiApiKey1 = defineSecret("GEMINI_API_KEY_1");
const geminiApiKey2 = defineSecret("GEMINI_API_KEY_2");

const GROQ_MODELS = [
  "llama-3.3-70b-versatile",
  "llama-3.1-8b-instant",
  "gemma2-9b-it",
];

const GEMINI_MODELS = [
  "gemini-2.0-flash",
  "gemini-2.0-flash-lite",
  "gemini-flash-latest",
];

async function tryGroq(model, messages, systemContext, apiKey) {
  const groqMessages = [];
  if (systemContext) {
    groqMessages.push({ role: "system", content: systemContext });
  }
  for (const msg of messages) {
    groqMessages.push({
      role: msg.role === "user" ? "user" : "assistant",
      content: msg.content,
    });
  }

  const res = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify({
      model,
      messages: groqMessages,
      temperature: 0.7,
      max_tokens: 1024,
    }),
  });

  const data = await res.json();
  if (data.error) return null;
  return data.choices?.[0]?.message?.content || null;
}

async function tryGemini(model, messages, systemContext, apiKey) {
  const contents = messages.map((msg) => ({
    role: msg.role === "user" ? "user" : "model",
    parts: [{ text: msg.content }],
  }));

  const body = { contents };
  if (systemContext) {
    body.system_instruction = { parts: [{ text: systemContext }] };
  }

  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-goog-api-key": apiKey,
    },
    body: JSON.stringify(body),
  });

  const data = await res.json();
  if (data.error) return null;
  return data.candidates?.[0]?.content?.parts?.[0]?.text || null;
}

exports.aiChat = onCall(
  {
    secrets: [groqApiKey, geminiApiKey1, geminiApiKey2],
    maxInstances: 10,
    timeoutSeconds: 60,
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Must be signed in.");
    }

    const { messages, systemContext } = request.data;
    if (!messages || !Array.isArray(messages)) {
      throw new HttpsError("invalid-argument", "messages is required.");
    }

    // Try Groq first
    const gKey = groqApiKey.value();
    if (gKey) {
      for (const model of GROQ_MODELS) {
        try {
          const result = await tryGroq(model, messages, systemContext, gKey);
          if (result) return { reply: result, provider: "groq", model };
        } catch (_) {}
      }
    }

    // Fall back to Gemini
    const geminiKeys = [geminiApiKey1.value(), geminiApiKey2.value()].filter(Boolean);
    for (const key of geminiKeys) {
      for (const model of GEMINI_MODELS) {
        try {
          const result = await tryGemini(model, messages, systemContext, key);
          if (result) return { reply: result, provider: "gemini", model };
        } catch (_) {}
      }
    }

    throw new HttpsError(
      "unavailable",
      "All AI providers are currently unavailable. Please try again."
    );
  }
);
