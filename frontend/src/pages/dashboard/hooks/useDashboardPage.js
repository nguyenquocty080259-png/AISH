import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";
import * as collectionApi from "../../../api/collectionApi";
import * as recentlyViewedApi from "../../../api/recentlyViewedApi";
import * as aiApi from "../../../api/aiApi";

export function useDashboardPage() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  const [collections, setCollections] = useState([]);
  const [recentlyViewed, setRecentlyViewed] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [collectionList, recentList, recommendationList] = await Promise.all([
        collectionApi.listMyCollections(),
        recentlyViewedApi.getRecentlyViewed(),
        aiApi.getRecommendationsForYou(),
      ]);
      setCollections(collectionList);
      setRecentlyViewed(recentList);
      setRecommendations(recommendationList);
    } catch (err) {
      setError(err.message);
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLogout = async () => {
    await logout();
    showSuccess(t("common.loggedOut"));
    navigate(ROUTES.HOME);
  };

  return {
    user,
    handleLogout,
    collections,
    recentlyViewed,
    recommendations,
    loading,
    error,
  };
}
