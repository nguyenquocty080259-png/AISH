import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as notificationApi from "../../../api/notificationApi";
import { useToast } from "../../../hooks/useToast";
import "./notification-settings.css";

const CONFIGURABLE_TYPES = ["COMMENT_ON_MY_DOC", "RATING_ON_MY_DOC", "DOCUMENT_SHARED", "CASE_REPLY"];

export default function NotificationSettings() {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();
  const [prefs, setPrefs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingType, setSavingType] = useState(null);

  useEffect(() => {
    let active = true;
    notificationApi.getNotificationPreferences()
      .then((data) => {
        if (!active) return;
        const list = Array.isArray(data) ? data : [];
        setPrefs(list.filter((pref) => CONFIGURABLE_TYPES.includes(pref.type)));
      })
      .catch((error) => active && showError(error.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleToggle = async (type) => {
    const previous = prefs;
    const next = prefs.map((pref) => (pref.type === type ? { ...pref, enabled: !pref.enabled } : pref));
    setPrefs(next);
    setSavingType(type);
    try {
      await notificationApi.updateNotificationPreferences(next);
      showSuccess(t("profile.notifPrefSaved"));
    } catch (error) {
      setPrefs(previous);
      showError(error.message);
    } finally {
      setSavingType(null);
    }
  };

  return (
    <div className="profile-storage">
      <h3 className="profile-storage__title">{t("profile.notifPrefTitle")}</h3>
      {loading ? (
        <p className="notification-settings__loading">{t("profile.loading")}</p>
      ) : (
        <div className="notification-settings">
          {prefs.map((pref) => (
            <label className="notification-settings__row" key={pref.type}>
              <span>{t(`profile.notifPref.${pref.type}`)}</span>
              <span className="notification-settings__switch">
                <input
                  type="checkbox"
                  checked={pref.enabled}
                  disabled={savingType === pref.type}
                  onChange={() => handleToggle(pref.type)}
                />
                <span className="notification-settings__slider" />
              </span>
            </label>
          ))}
        </div>
      )}
    </div>
  );
}
