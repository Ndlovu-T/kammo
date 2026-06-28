import { useEffect, useRef } from "react";
import { useKammo } from "../KammoContext";
import { useToast } from "./Toast";

export default function ErrorToastBridge() {
  const { error } = useKammo();
  const { showToast } = useToast();
  const lastShown = useRef("");

  useEffect(() => {
    if (error && error !== lastShown.current) {
      showToast(error, "error");
    }
    lastShown.current = error;
  }, [error, showToast]);

  return null;
}
