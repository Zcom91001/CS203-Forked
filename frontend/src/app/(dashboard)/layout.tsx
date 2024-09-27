"use client";

import Menu from "@/components/Menu";
import Navbar from "@/components/Navbar";
import Image from "next/image";
import Link from "next/link";

export default function DashboardLayout({
	children,
}: Readonly<{
	children: React.ReactNode;
}>) {
	return (
		<div className="h-screen flex">
			{/* LEFT */}
			<div className="w-[14%] md:w-[8%] lg:w-[16%] xl:w-[14%] p-4">
				<Link
					href="/"
					className="flex items-center justify-center gap-2 lg:justify-start p-2"
				>
					<Image src="/logo.svg" alt="logo" width={32} height={32} />
					<span className="hidden lg:block">TournaX</span>
				</Link>
				<Menu makeOthersVisible={true} />
			</div>

			{/* RIGHT */}
			<div className="w-[86%] md:w-[92%] lg:w-[84%] xl:w-[86%] bg-[#F7F8FA] overflow-y-scroll">
				<Navbar />
				{children}
			</div>
		</div>
	);
}