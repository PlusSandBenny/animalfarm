const BASE_URL = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");
const TOKEN_KEY = "animalfarm_token";
const USER_KEY = "animalfarm_user";

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

function authHeaders(extra = {}) {
  const token = getToken();
  return {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...extra
  };
}

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  const body = options.body;
  if (!(body instanceof FormData) && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: authHeaders(headers)
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Request failed: ${res.status}`);
  }
  if (res.status === 204) return null;
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.blob();
}

export const authApi = {
  async login(username, password) {
    const data = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(data));
    return data;
  },
  async logout() {
    try {
      await request("/auth/logout", { method: "POST" });
    } finally {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  },
  currentUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
};

export const api = {
  getAnimals: () => request("/animals"),
  registerOwner: (payload) => request("/owners", { method: "POST", body: JSON.stringify(payload) }),
  registerAnimal: (payload) => request("/animals", { method: "POST", body: JSON.stringify(payload) }),
  transferAnimals: (payload) => request("/animals/transfer", { method: "POST", body: JSON.stringify(payload) }),
  sellAnimal: (animalId) => request(`/animals/${animalId}/sell`, { method: "POST" }),
  createTransferRequest: (payload) => request("/transfer-requests", { method: "POST", body: JSON.stringify(payload) }),
  listTransferRequests: () => request("/transfer-requests"),
  approveTransferRequest: (id) => request(`/transfer-requests/${id}/approve`, { method: "POST" }),
  rejectTransferRequest: (id) => request(`/transfer-requests/${id}/reject`, { method: "POST" }),
  getOwner: (ownerId) => request(`/owners/${ownerId}`),
  downloadReport: async (type, value) => {
    let path = `/reports/owner/${value}`;
    if (type === "ownerVsAnimal") path = `/reports/owner-vs-animal?ownerId=${value}`;
    if (type === "parentVsAnimal") path = `/reports/parent-vs-animal?parentId=${value}`;
    return request(path);
  }
};
