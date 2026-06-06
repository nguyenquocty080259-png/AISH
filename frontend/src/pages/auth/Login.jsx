import "./Login.css";
import { FcGoogle } from "react-icons/fc";
import { useState } from "react";

const Login = () => {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {

    try {

      const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email,
            password,
          }),
        }
      );

      if (!response.ok) {
        alert("Login failed");
        return;
      }

      const data = await response.json();

      console.log(data);

      localStorage.setItem(
        "accessToken",
        data.accessToken
      );

      localStorage.setItem(
        "refreshToken",
        data.refreshToken
      );

      alert("Login success");

      // sau này chuyển sang dashboard
      // navigate("/dashboard");

    } catch (error) {
      console.error(error);
      alert("Server error");
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        <div className="login-left">
          <img
            src="/login.jpg"
            alt="StudyHub"
            className="login-image"
          />

          <h1 className="login-title">StudyHub AI</h1>

          <p className="login-subtitle">
            Your intellectual partner for visionary learning.
            <br />
            Accelerate your academic journey with the power of AI.
          </p>
        </div>

        <div className="login-right">
          <div className="form-box">

            <h2 className="form-title">
              Student Login
            </h2>

            <p className="form-subtitle">
              Welcome back! Please enter your details.
            </p>

            <div className="input-group">
              <label>Email</label>

              <input
                type="email"
                placeholder="name@university.edu"
                value={email}
                onChange={(e) =>
                  setEmail(e.target.value)
                }
              />
            </div>

            <div className="input-group">
              <label>Password</label>

              <input
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) =>
                  setPassword(e.target.value)
                }
              />
            </div>

            <button
              className="login-btn"
              onClick={handleLogin}
            >
              Log In
            </button>

            <div className="social-row">

              <button className="signUp-btn google-btn">
                <FcGoogle className="google-icon" />
                Google
              </button>

              <button className="signUp-btn">
                Sign up
              </button>

            </div>

          </div>
        </div>
      </div>

      <div className="footer">
        <span className="line1">
          © 2026 StudyHub AI
        </span>

        <span className="line2">
          Privacy Policy | Terms of Service
        </span>
      </div>
    </div>
  );
};

export default Login;