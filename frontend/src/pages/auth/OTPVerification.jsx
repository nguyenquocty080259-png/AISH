import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "./OTPVerification.css";

import {
  MdVerifiedUser,
  MdSchedule,
  MdArrowForward,
  MdWest,
} from "react-icons/md";

const OTPVerification = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const email =
  location.state?.email || localStorage.getItem("verifyEmail");

  const [otp, setOtp] = useState(["", "", "", "", "", ""]);
  const [timer, setTimer] = useState(120);
  const [loading, setLoading] = useState(false);

  const inputRefs = useRef([]);

  useEffect(() => {
    if (!email) {
      navigate("/signup");
    }
  }, [email, navigate]);

  useEffect(() => {
    const interval = setInterval(() => {
      setTimer((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;

    return `${mins.toString().padStart(2, "0")}:${secs
      .toString()
      .padStart(2, "0")}`;
  };

  const handleChange = (index, value) => {
    if (isNaN(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);

    setOtp(newOtp);

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const otpCode = otp.join("");

    if (timer <= 0) {
      alert("OTP expired. Please resend OTP.");
      return;
    }

    if (otpCode.length !== 6) {
      alert("Please enter all 6 OTP digits");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/verify-otp",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: email,
            otp: otpCode,
          }),
        }
      );

      if (response.ok) {
        alert("OTP verified successfully!");

        navigate("/login");
      } else {
        const errorMessage = await response.text();

        alert(errorMessage || "Invalid OTP");
      }
    } catch (error) {
      console.error(error);

      alert("Cannot connect to server");
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/resend-otp",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: email,
          }),
        }
      );

      if (response.ok) {
        alert("A new OTP has been sent!");

        setTimer(120);

        setOtp(["", "", "", "", "", ""]);

        inputRefs.current[0]?.focus();
      } else {
        alert("Failed to resend OTP");
      }
    } catch (error) {
      console.error(error);

      alert("Cannot connect to server");
    }
  };

  return (
    <div className="otp-page">
      <div className="otp-container">
        <div className="otp-brand">
          <h1>AI Study Hub</h1>
          <p>VISIONARY ACADEMIC TECH</p>
        </div>

        <div className="otp-card">
          <div className="otp-icon">
            <MdVerifiedUser />
          </div>

          <h2>OTP Verification</h2>

          <p className="otp-subtitle">
            We’ve sent a 6-digit verification code
            <br />
            to:
            <br />
            <strong>{email}</strong>
          </p>

          <form onSubmit={handleSubmit}>
            <div className="otp-inputs">
              {otp.map((digit, index) => (
                <input
                  key={index}
                  ref={(el) => (inputRefs.current[index] = el)}
                  type="text"
                  maxLength="1"
                  value={digit}
                  onChange={(e) =>
                    handleChange(index, e.target.value)
                  }
                  onKeyDown={(e) =>
                    handleKeyDown(index, e)
                  }
                />
              ))}
            </div>

            <div className="otp-timer">
              <div>
                <MdSchedule />
                {formatTime(timer)}
              </div>

              {timer === 0 && (
                <p className="expired-message">
                  OTP expired. Please resend OTP.
                </p>
              )}

              <p>
                Didn’t receive the code?
                <button
                  type="button"
                  onClick={handleResendOtp}
                  disabled={timer > 0}
                >
                  Resend code
                </button>
              </p>
            </div>

            <button
              className="otp-btn"
              type="submit"
              disabled={loading}
            >
              {loading
                ? "Verifying..."
                : "Verify & Continue"}

              <MdArrowForward />
            </button>
          </form>

          <button
            className="otp-back"
            type="button"
            onClick={() => navigate("/login")}
          >
            <MdWest />
            Back to login
          </button>
        </div>

        <div className="otp-footer">
          © 2026 AI Study Hub
        </div>
      </div>
    </div>
  );

};

export default OTPVerification;