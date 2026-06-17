import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";

import {
  getProfile,
} from "../../services/profileService";

export default function Profile() {
  const { user, logout } = useAuth();

  const [profile, setProfile] = useState(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const fetchProfile = useCallback(async () => {
    try {
      setLoading(true);

      const data =
        await getProfile();

      setProfile(data);
    } catch (err) {
      setError(
        err.message ||
          "Cannot load profile"
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const handleLogout =
    async () => {
      await logout();
    };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading profile...
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="bg-red-100 text-red-600 p-4 rounded">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-6xl mx-auto">

      {/* Header */}

      <div className="bg-white rounded-2xl shadow p-8 mb-8">

        <div className="flex items-center gap-6">

          <img
            src={
              profile?.avatarUrl ||
              "/default-avatar.png"
            }
            alt="avatar"
            className="w-32 h-32 rounded-full object-cover border"
          />

          <div>

            <h1 className="text-3xl font-bold">
              {profile?.fullName ||
                "Unknown User"}
            </h1>

            <p className="text-gray-500">
              @{profile?.username}
            </p>

            <p className="text-gray-500">
              {user?.email}
            </p>

          </div>

        </div>

      </div>

      {/* Personal Information */}

      <div className="bg-white rounded-2xl shadow p-8 mb-8">

        <h2 className="text-xl font-semibold mb-6">
          Personal Information
        </h2>

        <div className="grid md:grid-cols-2 gap-6">

          <InfoItem
            label="Full Name"
            value={profile?.fullName}
          />

          <InfoItem
            label="Phone Number"
            value={profile?.phoneNumber}
          />

          <InfoItem
            label="Date Of Birth"
            value={profile?.dob}
          />

          <InfoItem
            label="Gender"
            value={profile?.gender}
          />

          <InfoItem
            label="Country"
            value={profile?.country}
          />

          <InfoItem
            label="City"
            value={profile?.city}
          />

        </div>

      </div>

      {/* Academic Information */}

      <div className="bg-white rounded-2xl shadow p-8 mb-8">

        <h2 className="text-xl font-semibold mb-6">
          Academic Information
        </h2>

        <div className="grid md:grid-cols-2 gap-6">

          <InfoItem
            label="University"
            value={profile?.university}
          />

          <InfoItem
            label="Faculty"
            value={profile?.faculty}
          />

          <InfoItem
            label="Major"
            value={profile?.major}
          />

        </div>

      </div>

      {/* Social */}

      <div className="bg-white rounded-2xl shadow p-8 mb-8">

        <h2 className="text-xl font-semibold mb-6">
          Social Links
        </h2>

        <div className="space-y-4">

          <InfoItem
            label="Github"
            value={profile?.githubUrl}
          />

          <InfoItem
            label="LinkedIn"
            value={profile?.linkedinUrl}
          />

        </div>

      </div>

      {/* Bio */}

      <div className="bg-white rounded-2xl shadow p-8 mb-8">

        <h2 className="text-xl font-semibold mb-4">
          Bio
        </h2>

        <p className="text-gray-700">
          {profile?.bio ||
            "No bio available"}
        </p>

      </div>

      {/* Actions */}

      <div className="flex gap-4">

        <button
          className="px-6 py-3 bg-blue-600 text-white rounded-lg"
        >
          Edit Profile
        </button>

        <button
          className="px-6 py-3 bg-gray-200 rounded-lg"
        >
          Change Avatar
        </button>

        <button
          onClick={handleLogout}
          className="px-6 py-3 bg-red-600 text-white rounded-lg"
        >
          Logout
        </button>

      </div>

    </div>
  );
}

function InfoItem({
  label,
  value,
}) {
  return (
    <div>
      <p className="text-sm text-gray-500">
        {label}
      </p>

      <p className="font-medium">
        {value || "Not provided"}
      </p>
    </div>
  );
}
