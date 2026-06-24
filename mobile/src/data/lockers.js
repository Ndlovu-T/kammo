/** Mock PUDO/locker network — stand-in until a real locker-locator endpoint exists. */
export const LOCKERS = [
  {
    id: "PUDO-CT-001",
    network: "PUDO",
    name: "PUDO Locker – Canal Walk",
    line1: "Canal Walk Shopping Centre, Century Blvd",
    city: "Cape Town",
    province: "Western Cape",
    postalCode: "7441",
  },
  {
    id: "PUDO-CT-014",
    network: "PUDO",
    name: "PUDO Locker – Cavendish Square",
    line1: "Dreyer St, Claremont",
    city: "Cape Town",
    province: "Western Cape",
    postalCode: "7708",
  },
  {
    id: "PARGO-JHB-002",
    network: "Pargo",
    name: "Pargo Point – Sandton City",
    line1: "Rivonia Rd, Sandhurst",
    city: "Johannesburg",
    province: "Gauteng",
    postalCode: "2196",
  },
  {
    id: "PARGO-JHB-019",
    network: "Pargo",
    name: "Pargo Point – Rosebank Mall",
    line1: "Cradock Ave, Rosebank",
    city: "Johannesburg",
    province: "Gauteng",
    postalCode: "2196",
  },
  {
    id: "PUDO-PTA-007",
    network: "PUDO",
    name: "PUDO Locker – Menlyn Park",
    line1: "Atterbury Rd, Menlyn",
    city: "Pretoria",
    province: "Gauteng",
    postalCode: "0181",
  },
  {
    id: "PARGO-DBN-005",
    network: "Pargo",
    name: "Pargo Point – Gateway Theatre of Shopping",
    line1: "1 Palm Blvd, Umhlanga",
    city: "Durban",
    province: "KwaZulu-Natal",
    postalCode: "4319",
  },
  {
    id: "PUDO-PE-003",
    network: "PUDO",
    name: "PUDO Locker – Baywest Mall",
    line1: "Capricorn Dr, Bay West",
    city: "Gqeberha",
    province: "Eastern Cape",
    postalCode: "6001",
  },
  {
    id: "PARGO-BFN-001",
    network: "Pargo",
    name: "Pargo Point – Mimosa Mall",
    line1: "Kellner St, Westdene",
    city: "Bloemfontein",
    province: "Free State",
    postalCode: "9301",
  },
];

export function findLocker(id) {
  return LOCKERS.find((l) => l.id === id) || null;
}

export function searchLockers(query) {
  const q = String(query || "").trim().toLowerCase();
  if (!q) return LOCKERS;
  return LOCKERS.filter((l) =>
    [l.name, l.city, l.province, l.network].some((field) => field.toLowerCase().includes(q))
  );
}
