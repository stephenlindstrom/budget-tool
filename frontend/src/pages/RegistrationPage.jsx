import { useState } from "react";
import api from "../api/api";
import { useAuth } from "../hooks/useAuth";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import PennyPincherLogo from "../components/PennyPincherLogo";

function RegistrationPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: "", password: "", confirmPassword: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [showPwd, setShowPwd] = useState(false);
  const [touchedConfirm, setTouchedConfirm] = useState(false);

  const MIN_USER = 3, MAX_USER = 50;
  const MIN_PW = 8, MAX_PW = 72;

  const hasUsername = form.username.trim().length >= MIN_USER && form.username.trim().length <= MAX_USER;
  const strongEnough = form.password.length >= MIN_PW && form.password.length <= MAX_PW;
  const passwordsMatch = form.password && form.password === form.confirmPassword;
  const canSubmit = hasUsername && passwordsMatch && strongEnough && !submitting;

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;

    setError("");
    setSubmitting(true);

    try {
      const res = await api.post("/auth/register", {
        username: form.username.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
      });
      const actualToken = res.data.token;
      login(actualToken);
      navigate("/dashboard");
    } catch (err) {
      console.error("Registration failed:", err?.response?.status, err);
      const status = err?.response?.status;
      const msg =
        err?.response?.data?.message || 
        (status === 409
          ? "Username already taken."
          : err?.code === "ERR_NETWORK"
          ? "Network error. Check your connection."
          : "Error registering user");
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center px-4">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <header className="mb-6 text-center">
          <PennyPincherLogo size={64} showWordmark={false} />
          <h1 className="text-2xl font-semibold text-slate-900">Create your account</h1>
          <p className="mt-1 text-sm text-slate-500">
            Already have one?{" "}
            <Link to="/login" className="font-medium text-blue-600 hover:text-blue-700">
              Log in
            </Link>
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

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Username */}
          <div>
            <label htmlFor="username" className="mb-1 block text-sm font-medium text-slate-700">
              Username
            </label>
            <input
              id="username"
              name="username"
              autoComplete="username"
              value={form.username}
              onChange={handleChange}
              minLength={MIN_USER}
              maxLength={MAX_USER}
              placeholder="Enter username"
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-slate-900 shadow-sm outline-none placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
              required
            />
            {!hasUsername && form.username && (
              <p className="text-sm text-red-600 mt-1">Username must be between {MIN_USER} and {MAX_USER} characters.</p>
            )}
          </div>

          {/* Passwords */}
          <div className="space-y-3">
            <div>
              <label htmlFor="password" className="mb-1 block text-sm font-medium text-slate-700">
                Password
              </label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPwd ? "text" : "password"}
                  autoComplete="new-password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Enter password"
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 pr-12 text-slate-900 shadow-sm outline-none placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
                  aria-describedby="pw-hint"
                  required
                  minLength={MIN_PW}
                  maxLength={MAX_PW}
                />
                <button
                  type="button"
                  onClick={() => setShowPwd((s) => !s)}
                  className="absolute inset-y-0 right-0 flex items-center justify-center px-3 text-slate-600 hover:text-slate-900 focus:outline-none cursor-pointer"
                  aria-pressed={showPwd}
                  aria-label={showPwd ? "Hide password" : "Show password"}
                >
                  {showPwd ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>

              {!strongEnough && form.password && (
                <p className="text-sm text-red-600 mt-1">Password must be between {MIN_PW} and {MAX_PW} characters.</p>
              )}
            </div>


            <div>
              <label htmlFor="confirmPassword" className="mb-1 block text-sm font-medium text-slate-700">
                Confirm password
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type={showPwd ? "text" : "password"}
                autoComplete="new-password"
                value={form.confirmPassword}
                onChange={handleChange}
                onBlur={() => setTouchedConfirm(true)}
                placeholder="Repeat your password"
                className={`w-full rounded-lg border bg-white px-3 py-2.5 text-slate-900 shadow-sm outline-none placeholder:text-slate-400 focus:ring-2 focus:ring-blue-500/20 ${
                  !passwordsMatch && touchedConfirm && form.confirmPassword
                    ? "border-red-400 focus:border-red-500 focus:ring-red-500/20"
                    : "border-slate-300 focus:border-blue-500"
                }`}
                aria-invalid={!passwordsMatch && touchedConfirm && form.confirmPassword ? "true" : "false"}
                aria-describedby={
                  !passwordsMatch && touchedConfirm && form.confirmPassword ? "confirm-error" : undefined
                }
                required
              />
              {!passwordsMatch && touchedConfirm && form.confirmPassword && (
                <p id="confirm-error" className="mt-1 text-sm text-red-600" role="alert" aria-live="polite">
                  Passwords do not match
                </p>
              )}
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={!canSubmit}
            className="inline-flex w-full items-center justify-center rounded-lg bg-blue-600 px-4 py-2.5 text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
          >
            {submitting ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4A4 4 0 004 12z" />
                </svg>
                Creating…
              </span>
            ) : (
              "Create account"
            )}
          </button>
        </form>
      </div>
    </div>
  );
}

export default RegistrationPage;