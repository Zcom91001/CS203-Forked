"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

const Homepage = () => {
  const router = useRouter();

  useEffect(() => {
    // Redirect to the new page
    router.replace("/login");
  }, [router]);

  return null;
};

export default Homepage;
