import { Outlet } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import Navbar from "../components/Navbar";

export default function ProtectedLayout() {
  return (
    <ProtectedRoute>
      <Navbar />
      <Outlet />
    </ProtectedRoute>
  );
}