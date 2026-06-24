/**
 * KAMMO REST API client — same contract as kammobackend Spring Boot API.
 * @param {string} apiBaseUrl e.g. http://192.168.1.8:8080
 */
export function createKammoApi(apiBaseUrl) {
  async function request(path, { method = "GET", token, body } = {}) {
    let response;
    try {
      response = await fetch(`${apiBaseUrl}${path}`, {
        method,
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        ...(body ? { body: JSON.stringify(body) } : {}),
      });
    } catch {
      throw new Error(
        `Cannot reach backend at ${apiBaseUrl}. Check Wi‑Fi and that the server is running.`
      );
    }

    const text = await response.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        throw new Error("Invalid response from server");
      }
    }

    if (!response.ok) {
      throw new Error(data?.message || `Request failed (${response.status})`);
    }

    return data;
  }

  return {
    ping: () => request("/api/health"),
    register: (payload) => request("/api/auth/register", { method: "POST", body: payload }),
    login: (payload) => request("/api/auth/login", { method: "POST", body: payload }),
    getProfile: (token) => request("/api/me/profile", { token }),
    getBankAccount: (token) => request("/api/me/bank-account", { token }),
    updateBankAccount: (token, payload) =>
      request("/api/me/bank-account", { method: "PUT", token, body: payload }),
    getDashboard: (token) => request("/api/dashboard", { token }),
    getMyDeals: (token, role) =>
      request(`/api/deals${role ? `?role=${encodeURIComponent(role)}` : ""}`, { token }),
    getSellerDeals: (token) => request("/api/deals/seller", { token }),
    createDeal: (token, payload) => request("/api/deals", { method: "POST", token, body: payload }),
    createSellerDeal: (token, payload) =>
      request("/api/deals/seller", { method: "POST", token, body: payload }),
    getDeal: (dealCode, token) => request(`/api/deals/${dealCode}`, { token }),
    markPaymentSecured: (token, dealCode) =>
      request(`/api/deals/${dealCode}/payment`, { method: "POST", token }),
    acceptAsSeller: (token, dealCode) =>
      request(`/api/deals/${dealCode}/seller/accept`, { method: "POST", token }),
    acceptAsBuyer: (token, dealCode, dealReference) =>
      request(`/api/deals/${dealCode}/buyer/accept`, {
        method: "POST",
        token,
        body: { dealReference },
      }),
    markReadyForCollection: (token, dealCode) =>
      request(`/api/deals/${dealCode}/seller/ready-for-collection`, { method: "POST", token }),
    markInTransit: (token, dealCode) =>
      request(`/api/deals/${dealCode}/ship`, { method: "POST", token }),
    confirmDelivery: (token, dealCode) =>
      request(`/api/deals/${dealCode}/confirm-delivery`, { method: "POST", token }),
    raiseDispute: (token, dealCode) =>
      request(`/api/deals/${dealCode}/disputes`, { method: "POST", token }),
    getMessages: (token, dealCode) => request(`/api/deals/${dealCode}/messages`, { token }),
    sendMessage: (token, dealCode, body) =>
      request(`/api/deals/${dealCode}/messages`, { method: "POST", token, body: { body } }),
    rateDeal: (token, dealCode, score, comment) =>
      request(`/api/deals/${dealCode}/ratings`, {
        method: "POST",
        token,
        body: { score, comment },
      }),
    getListings: (token, { category = "", search = "" } = {}) => {
      const params = new URLSearchParams();
      if (category) params.set("category", category);
      if (search) params.set("search", search);
      const query = params.toString();
      return request(`/api/marketplace/listings${query ? `?${query}` : ""}`, { token });
    },
    getListing: (token, id) => request(`/api/marketplace/listings/${id}`, { token }),
  };
}

export function mergeDeals(owned = [], asSeller = []) {
  const map = new Map();
  [...owned, ...asSeller].forEach((deal) => map.set(deal.dealCode, deal));
  return Array.from(map.values());
}
