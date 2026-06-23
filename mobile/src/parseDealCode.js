export function parseDealCode(raw) {
  const text = String(raw || "").trim();
  if (!text) return "";

  const fromUrl = text.match(/deal\/([A-Za-z0-9-]+)/i);
  if (fromUrl) return fromUrl[1].replace(/^KM-/i, "");

  const kmMatch = text.match(/KM-([A-Za-z0-9]+)/i);
  if (kmMatch) return kmMatch[1];

  return text.replace(/\s/g, "");
}
