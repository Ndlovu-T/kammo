import AsyncStorage from "@react-native-async-storage/async-storage";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { kammoApi, mergeDeals } from "./api";
import { openCheckout } from "./checkout";
import { validateCreateDraft } from "./dealActions";
import {
  addressToPayload,
  defaultCreateDraft,
  deliveryToApi,
  lockerToAddressPayload,
  normalizePhone,
} from "./utils";
import {
  buildWalletSnapshot,
  loadWalletBalance,
  saveWalletBalance,
} from "./wallet";

const STORAGE_KEY = "kammo_token";

const defaultDraft = { ...defaultCreateDraft };

const KammoContext = createContext(null);

export function KammoProvider({ children }) {
  const [ready, setReady] = useState(false);
  const [token, setToken] = useState("");
  const [auth, setAuth] = useState(null);
  const [profile, setProfile] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [bankAccount, setBankAccount] = useState(null);
  const [deals, setDeals] = useState([]);
  const [listings, setListings] = useState([]);
  const [selectedDeal, setSelectedDeal] = useState(null);
  const [selectedListing, setSelectedListing] = useState(null);
  const [messages, setMessages] = useState([]);
  const [error, setError] = useState("");
  const [sessionError, setSessionError] = useState("");
  const [backendOk, setBackendOk] = useState(null);
  const [busy, setBusy] = useState(false);
  const [createDraft, setCreateDraft] = useState(defaultDraft);
  const [walletBalance, setWalletBalance] = useState(0);
  const [backendWallet, setBackendWallet] = useState(null);

  useEffect(() => {
    Promise.all([AsyncStorage.getItem(STORAGE_KEY), loadWalletBalance()]).then(
      ([stored, balance]) => {
        if (stored) setToken(stored);
        setWalletBalance(balance);
        setReady(true);
      }
    );
  }, []);

  const checkBackend = useCallback(async () => {
    try {
      await kammoApi.ping();
      setBackendOk(true);
      return true;
    } catch (e) {
      setBackendOk(false);
      setSessionError(e.message);
      return false;
    }
  }, []);

  useEffect(() => {
    checkBackend();
  }, [checkBackend]);

  const setTokenPersist = useCallback(async (value) => {
    setToken(value || "");
    if (value) await AsyncStorage.setItem(STORAGE_KEY, value);
    else await AsyncStorage.removeItem(STORAGE_KEY);
  }, []);

  const withBusy = useCallback(async (fn) => {
    setError("");
    setBusy(true);
    try {
      return await fn();
    } catch (e) {
      setError(e.message || "Something went wrong");
      throw e;
    } finally {
      setBusy(false);
    }
  }, []);

  const refreshSession = useCallback(
    async (activeToken = token) => {
      if (!activeToken) return;
      setSessionError("");
      try {
        const [prof, dash, owned, asSeller, market, bank, wallet] = await Promise.all([
          kammoApi.getProfile(activeToken),
          kammoApi.getDashboard(activeToken),
          kammoApi.getMyDeals(activeToken),
          kammoApi.getSellerDeals(activeToken),
          kammoApi.getListings(activeToken),
          kammoApi.getBankAccount(activeToken),
          kammoApi.getWallet(activeToken),
        ]);
        setProfile(prof);
        setDashboard(dash);
        setDeals(mergeDeals(owned, asSeller));
        setListings(market);
        setBankAccount(bank);
        setBackendWallet(wallet);
        setBackendOk(true);
      } catch (e) {
        setSessionError(e.message || "Failed to sync with backend");
        setBackendOk(false);
        throw e;
      }
    },
    [token]
  );

  useEffect(() => {
    if (ready && token) refreshSession().catch(() => {});
  }, [ready, token, refreshSession]);

  const register = useCallback(
    async ({ phone, email, password }) =>
      withBusy(async () => {
        const res = await kammoApi.register({
          phoneNumber: normalizePhone(phone),
          email,
          password,
        });
        setAuth(res);
        await setTokenPersist(res.token);
        await refreshSession(res.token);
        setBackendOk(true);
        return res;
      }),
    [refreshSession, setTokenPersist, withBusy]
  );

  const login = useCallback(
    async ({ phone, password }) =>
      withBusy(async () => {
        const res = await kammoApi.login({
          phoneNumber: normalizePhone(phone),
          password,
        });
        setAuth(res);
        await setTokenPersist(res.token);
        await refreshSession(res.token);
        setBackendOk(true);
        return res;
      }),
    [refreshSession, setTokenPersist, withBusy]
  );

  const logout = useCallback(async () => {
    setAuth(null);
    setProfile(null);
    setDashboard(null);
    setBankAccount(null);
    setBackendWallet(null);
    setDeals([]);
    setListings([]);
    setSelectedDeal(null);
    setSelectedListing(null);
    setMessages([]);
    setCreateDraft(defaultDraft);
    setSessionError("");
    await setTokenPersist("");
  }, [setTokenPersist]);

  const loadListings = useCallback(
    async ({ category = "", search = "" } = {}) => {
      if (!token) return [];
      return withBusy(async () => {
        const data = await kammoApi.getListings(token, { category, search });
        setListings(data);
        return data;
      });
    },
    [token, withBusy]
  );

  const openListing = useCallback(
    async (id) => {
      if (!token) return null;
      return withBusy(async () => {
        const listing = await kammoApi.getListing(token, id);
        setSelectedListing(listing);
        return listing;
      });
    },
    [token, withBusy]
  );

  const loadDeal = useCallback(
    async (dealCode, { withMessages = true } = {}) => {
      if (!dealCode) return null;
      return withBusy(async () => {
        const deal = await kammoApi.getDeal(dealCode, token || undefined);
        setSelectedDeal(deal);
        if (withMessages && token) {
          setMessages(await kammoApi.getMessages(token, dealCode));
        }
        return deal;
      });
    },
    [token, withBusy]
  );

  const selectDeal = useCallback(
    async (dealCode) => loadDeal(dealCode, { withMessages: true }),
    [loadDeal]
  );

  const refreshMessages = useCallback(async () => {
    if (!token || !selectedDeal?.dealCode) return;
    const msgs = await kammoApi.getMessages(token, selectedDeal.dealCode);
    setMessages(msgs);
    return msgs;
  }, [selectedDeal, token]);

  const submitCreateDeal = useCallback(async () => {
    return withBusy(async () => {
      if (!token) throw new Error("Not signed in");
      const validationError = validateCreateDraft(createDraft);
      if (validationError) throw new Error(validationError);

      const isLocker = createDraft.delivery === "locker";
      const collectionAddress = isLocker
        ? lockerToAddressPayload(createDraft.collectionLocker)
        : addressToPayload(createDraft.collectionAddress);
      const deliveryAddress = isLocker
        ? lockerToAddressPayload(createDraft.deliveryLocker)
        : addressToPayload(createDraft.deliveryAddress);

      const payload = {
        itemName: createDraft.itemName.trim(),
        price: Number(createDraft.price),
        description: createDraft.description.trim(),
        deliveryMethod: deliveryToApi(createDraft.delivery),
        inspectionWindowHours: Number(createDraft.inspectionHours),
        collectionAddress,
        deliveryAddress,
      };

      const deal =
        createDraft.role === "seller"
          ? await kammoApi.createSellerDeal(token, {
              ...payload,
              buyerPhoneNumber: normalizePhone(createDraft.otherPhone),
            })
          : await kammoApi.createDeal(token, {
              ownerRole: "BUYER",
              otherPartyPhoneNumber: normalizePhone(createDraft.otherPhone),
              ...payload,
            });
      setSelectedDeal(deal);
      await refreshSession();
      return deal;
    });
  }, [createDraft, refreshSession, token, withBusy]);

  const dealAction = useCallback(
    async (fn) => {
      if (!token || !selectedDeal?.dealCode) throw new Error("No deal selected");
      return withBusy(async () => {
        const deal = await fn(token, selectedDeal.dealCode);
        setSelectedDeal(deal);
        await refreshSession();
        return deal;
      });
    },
    [refreshSession, selectedDeal, token, withBusy]
  );

  const acceptAsBuyer = useCallback(async () => {
    if (!selectedDeal?.dealReference) throw new Error("No deal selected");
    return dealAction((t, code) =>
      kammoApi.acceptAsBuyer(t, code, selectedDeal.dealReference)
    );
  }, [dealAction, selectedDeal]);

  const sendMessage = useCallback(
    async (body) => {
      if (!token || !selectedDeal?.dealCode) return;
      return withBusy(async () => {
        await kammoApi.sendMessage(token, selectedDeal.dealCode, body);
        await refreshMessages();
      });
    },
    [refreshMessages, selectedDeal, token, withBusy]
  );

  const rateDeal = useCallback(
    async (score, comment) => {
      if (!token || !selectedDeal?.dealCode) return;
      return withBusy(async () => {
        await kammoApi.rateDeal(token, selectedDeal.dealCode, score, comment);
        await refreshSession();
      });
    },
    [refreshSession, selectedDeal, token, withBusy]
  );

  const userPhone = auth?.phoneNumber || profile?.phoneNumber || "";

  const displayName = useMemo(() => {
    if (profile?.displayName) return profile.displayName;
    if (auth?.email) return auth.email.split("@")[0];
    return "Trader";
  }, [auth, profile]);

  const wallet = useMemo(
    () =>
      buildWalletSnapshot({
        wallet: backendWallet,
        profile,
        dashboard,
        deals,
      }),
    [backendWallet, profile, dashboard, deals]
  );

  const addFunds = useCallback(
    async (amount) => {
      return withBusy(async () => {
        const n = Number(amount);
        if (!n || n < 1) throw new Error("Enter a valid amount");
        if (!token) throw new Error("Not signed in");

        const { status, checkoutUrl, reference } = await kammoApi.initiateWalletTopup(token, n);
        if (status === "PENDING" && checkoutUrl) {
          await openCheckout(checkoutUrl, "kammo://wallet/callback");
        }
        const wallet = await kammoApi.verifyWalletTopup(token, reference);
        setBackendWallet(wallet);
        await refreshSession();
        return wallet.balance;
      });
    },
    [refreshSession, token, withBusy]
  );

  const withdrawFunds = useCallback(
    async (amount) => {
      return withBusy(async () => {
        const n = Number(amount);
        if (!n || n < 1) throw new Error("Enter a valid amount");
        if (n > walletBalance) throw new Error("Insufficient wallet balance");
        if (!bankAccount?.configured) throw new Error("Add a bank account before withdrawing");
        const next = walletBalance - n;
        await saveWalletBalance(next);
        setWalletBalance(next);
        return next;
      });
    },
    [bankAccount, walletBalance, withBusy]
  );

  const saveBankAccount = useCallback(
    async ({ bankCode, bankAccountNumber, bankAccountName }) => {
      if (!token) throw new Error("Not signed in");
      return withBusy(async () => {
        const account = await kammoApi.updateBankAccount(token, {
          bankCode: bankCode.trim(),
          bankAccountNumber: bankAccountNumber.trim(),
          bankAccountName: bankAccountName.trim(),
        });
        setBankAccount(account);
        return account;
      });
    },
    [token, withBusy]
  );

  return (
    <KammoContext.Provider
      value={{
        ready,
        token,
        auth,
        profile,
        dashboard,
        bankAccount,
        deals,
        listings,
        selectedDeal,
        selectedListing,
        messages,
        error,
        sessionError,
        backendOk,
        busy,
        createDraft,
        setCreateDraft,
        displayName,
        userPhone,
        wallet,
        walletBalance,
        addFunds,
        withdrawFunds,
        saveBankAccount,
        register,
        login,
        logout,
        checkBackend,
        refreshSession,
        loadListings,
        openListing,
        loadDeal,
        selectDeal,
        refreshMessages,
        submitCreateDeal,
        dealAction,
        acceptAsBuyer,
        sendMessage,
        rateDeal,
        clearError: () => setError(""),
        kammoApi,
      }}
    >
      {children}
    </KammoContext.Provider>
  );
}

export function useKammo() {
  const ctx = useContext(KammoContext);
  if (!ctx) throw new Error("useKammo must be used within KammoProvider");
  return ctx;
}
