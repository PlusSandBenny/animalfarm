const BASE_URL = "http://localhost:8080/api";

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Request failed: ${res.status}`);
  }
  if (res.status === 204) return null;
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.blob();
}

export const api = {
  getAnimals: () => request("/animals"),
  registerOwner: (payload, actorRole = "ADMIN") =>
    request("/owners", { method: "POST", headers: { "X-Actor-Role": actorRole }, body: JSON.stringify(payload) }),
  registerAnimal: (payload) => request("/animals", { method: "POST", body: JSON.stringify(payload) }),
  transferAnimals: (payload) => request("/animals/transfer", { method: "POST", body: JSON.stringify(payload) }),
  sellAnimal: (animalId, actorRole = "ADMIN") =>
    request(`/animals/${animalId}/sell`, { method: "POST", body: JSON.stringify({ actorRole }) }),
  createTransferRequest: (payload) =>
    request("/transfer-requests", { method: "POST", body: JSON.stringify(payload) }),
  listTransferRequests: () => request("/transfer-requests"),
  approveTransferRequest: (id, actorRole = "ADMIN") =>
    request(`/transfer-requests/${id}/approve`, { method: "POST", body: JSON.stringify({ actorRole }) }),
  rejectTransferRequest: (id, actorRole = "ADMIN") =>
    request(`/transfer-requests/${id}/reject`, { method: "POST", body: JSON.stringify({ actorRole }) }),
  reportUrl: (type, value) => {
    if (type === "ownerVsAnimal") return `${BASE_URL}/reports/owner-vs-animal?ownerId=${value}`;
    if (type === "parentVsAnimal") return `${BASE_URL}/reports/parent-vs-animal?parentId=${value}`;
    return `${BASE_URL}/reports/owner/${value}`;
  }
};
