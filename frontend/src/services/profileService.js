import API_BASE_URL from "../api/api";
import {
  getAccessToken,
} from "./authService";

const PROFILE_URL =
  `${API_BASE_URL}/api/profile`;

// ======================================================
// Helper
// ======================================================

const authHeader = () => ({
  Authorization:
    `Bearer ${getAccessToken()}`,
});

// ======================================================
// GET PROFILE
// GET /api/profile/me
// ======================================================

export const getProfile = async () => {
  const res = await fetch(
    `${PROFILE_URL}/me`,
    {
      headers: authHeader(),
    }
  );

  const data = await res.json();

  if (!res.ok) {
    throw new Error(
      data.message ||
        "Failed to load profile"
    );
  }

  return data;
};

// ======================================================
// UPDATE PROFILE
// PUT /api/profile/me
// ======================================================

export const updateProfile =
  async (profileData) => {
    const res = await fetch(
      `${PROFILE_URL}/me`,
      {
        method: "PUT",
        headers: {
          ...authHeader(),
          "Content-Type":
            "application/json",
        },
        body: JSON.stringify(
          profileData
        ),
      }
    );

    const data = await res.json();

    if (!res.ok) {
      throw new Error(
        data.message ||
          "Update profile failed"
      );
    }

    return data;
  };

// ======================================================
// UPLOAD AVATAR
// POST /api/profile/avatar
// ======================================================

export const uploadAvatar =
  async (file) => {
    const formData = new FormData();

    formData.append("file", file);

    const res = await fetch(
      `${PROFILE_URL}/avatar`,
      {
        method: "POST",
        headers: authHeader(),
        body: formData,
      }
    );

    const data = await res.json();

    if (!res.ok) {
      throw new Error(
        data.message ||
          "Upload avatar failed"
      );
    }

    return data;
  };

// ======================================================
// DELETE AVATAR
// DELETE /api/profile/avatar
// ======================================================

export const deleteAvatar =
  async () => {
    const res = await fetch(
      `${PROFILE_URL}/avatar`,
      {
        method: "DELETE",
        headers: authHeader(),
      }
    );

    if (!res.ok) {
      throw new Error(
        "Delete avatar failed"
      );
    }

    return true;
  };

// ======================================================
// UPLOAD BANNER
// POST /api/profile/banner
// ======================================================

export const uploadBanner =
  async (file) => {
    const formData = new FormData();

    formData.append("file", file);

    const res = await fetch(
      `${PROFILE_URL}/banner`,
      {
        method: "POST",
        headers: authHeader(),
        body: formData,
      }
    );

    const data = await res.json();

    if (!res.ok) {
      throw new Error(
        data.message ||
          "Upload banner failed"
      );
    }

    return data;
  };

// ======================================================
// DELETE BANNER
// DELETE /api/profile/banner
// ======================================================

export const deleteBanner =
  async () => {
    const res = await fetch(
      `${PROFILE_URL}/banner`,
      {
        method: "DELETE",
        headers: authHeader(),
      }
    );

    if (!res.ok) {
      throw new Error(
        "Delete banner failed"
      );
    }

    return true;
  };