import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/api";
import { useAuth } from "../hooks/useAuth";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import PennyPincherLogo from "../components/PennyPincherLogo";

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [showPwd, setShowPwd] = useState(false);

  const canSubmit = form.username.trim().length > 0 && form.password && !submitting;

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    setError("");
    setSubmitting(true);

    try {
      const response = await api.post("/auth/login", form);
      const actualToken = response.data.token;
      login(actualToken);
      navigate("/dashboard");
    } catch (err) {
      console.error(err);
      setError("Invalid username or password");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-xl bg-white p-6 shadow">
        <header className="mb-6 text-center">
          <PennyPincherLogo size={64} showWordmark={false} />
          <h2 className="text-2xl font-semibold text-slate-900">Login</h2>
          <p className="mt-1 text-sm text-slate-600">
            Welcome back! Please sign in to continue.
          </p>
        </header>

        {error && (
          <div
            role="alert"
            className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid gap-4">
          <div>
            <label
              htmlFor="username"
              className="mb-1 block text-sm font-medium text-slate-800"
            >
              Username
            </label>
            <input
              id="username"
              type="text"
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="Enter username"
              required
              autoComplete="username"
              className="h-10 w-full rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="mb-1 block text-sm font-medium text-slate-800"
            >
              Password
            </label>
            <div className="relative">
              <input
                id="password"
                type={showPwd ? "text" : "password"}
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Enter password"
                required
                autoComplete="current-password"
                className="h-10 w-full rounded-md border border-slate-300 bg-white px-3 pr-12 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
              />
              <button
                type="button"
                onClick={() => setShowPwd((v) => !v)}
                className="absolute inset-y-0 right-0 flex items-center justify-center px-3 text-slate-600 hover:text-slate-900 focus:outline-none cursor-pointer"
                aria-pressed={showPwd}
                aria-label={showPwd ? "Hide password" : "Show password"}
              >
                {showPwd ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={!canSubmit}
            className="mt-2 h-10 w-full rounded-md bg-blue-600 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60 cursor-pointer"
          >
            {submitting ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4A4 4 0 004 12z" />
                </svg>
                Logging in...
              </span>
            ) : (
              "Submit"
            )}
          </button>
        </form>

        <p className="mt-1 text-sm text-slate-600 text-center">
          Don’t have an account?{" "}
          <Link to="/register" className="font-medium text-blue-600 hover:text-blue-700">
            Sign up
          </Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;