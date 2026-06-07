import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Signup.css";
import { FcGoogle } from "react-icons/fc";
import {
  MdVisibility,
  MdVisibilityOff,
  MdSms,
} from "react-icons/md";

const SignUp = () => {
  const navigate = useNavigate();

  const [showPassword, setShowPassword] = useState(false);

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    agreeTerms: false,
  });

  const handleChange = (e) => {
    const { name, value, checked, type } = e.target;

    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.agreeTerms) {
      alert("Please agree to Terms & Conditions");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/signup",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            fullName: formData.fullName,
            email: formData.email,
           // phoneNumber: formData.phone,
            password: formData.password,
          }),
        }
      );

      const text = await response.text();

      console.log("Response:", text);

      if (response.ok) {

        localStorage.setItem(
          "verifyEmail",
          formData.email
        );

        // LỖI 2 FIX: thêm state: { email } để OTPVerification
        // nhận được email qua location.state (không chỉ localStorage)
        navigate("/otp-verification", {
          state: { email: formData.email },
        });
      }
    } catch (error) {
      console.error(error);
      alert("Cannot connect to server");
    }
  };

  return (
    <div className="signup-page">
      <div className="signup-card">

        <div className="signup-left">
          <img
            src="/signup.jpg"
            alt="signup"
            className="signup-image"
          />

          <h1 className="signup-title">
            AI Study Hub
          </h1>

          <p className="signup-subtitle">
            Join thousands of students accelerating
            <br />
            their academic journey through AI-powered
            <br />
            intelligent learning.
          </p>
        </div>

        <div className="signup-right">
          <div className="signup-form-box">

            <h2>Create Account</h2>

            <div className="signup-social-row">
              <button
                type="button"
                className="signup-social-btn"
              >
                <MdSms />
                OTP SMS
              </button>

              <button
                type="button"
                className="signup-social-btn"
              >
                <FcGoogle />
                Google
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="signup-input-group">
                <input
                  type="text"
                  name="fullName"
                  placeholder="Full Name"
                  value={formData.fullName}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="signup-input-group">
                <input
                  type="email"
                  name="email"
                  placeholder="Email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>

              {/* <div className="signup-input-group">
                <input
                  type="tel"
                  name="phone"
                  placeholder="Phone Number"
                  value={formData.phone}
                  onChange={handleChange}
                />
              </div> */}

              <div className="signup-input-group password-box">
                <input
                  type={
                    showPassword
                      ? "text"
                      : "password"
                  }
                  name="password"
                  placeholder="Password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                />

                <button
                  type="button"
                  className="toggle-btn"
                  onClick={() =>
                    setShowPassword(!showPassword)
                  }
                >
                  {showPassword ? (
                    <MdVisibilityOff />
                  ) : (
                    <MdVisibility />
                  )}
                </button>
              </div>

              <div className="signup-input-group">
                <input
                  type="password"
                  name="confirmPassword"
                  placeholder="Confirm Password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="signup-terms">
                <input
                  type="checkbox"
                  name="agreeTerms"
                  checked={formData.agreeTerms}
                  onChange={handleChange}
                />
                <span>
                  I agree to Terms of Service
                </span>
              </div>

              <button
                type="submit"
                className="signup-btn"
              >
                Create Account
              </button>
            </form>

            <p className="signup-login-link">
              Already have an account?
              <span> Log in</span>
            </p>

          </div>
        </div>

      </div>
    </div>
  );
};

export default SignUp;
