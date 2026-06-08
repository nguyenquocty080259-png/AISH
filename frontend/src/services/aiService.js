// aiService.js hiện tại
const API_URL = "http://localhost:8080/api/ai/chat";

// Nên dùng environment variable
const API_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

export async function askAI(message, documentId = null, conversationId = null) {
  const response = await fetch(`${API_URL}/api/ai/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message, documentId, conversationId }),
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}