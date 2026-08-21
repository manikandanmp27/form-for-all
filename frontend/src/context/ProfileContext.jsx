import React, { createContext, useContext, useState, useEffect } from 'react';
import { profileApi } from '../api/profileApi';
import { useAuth } from './AuthContext';

const ProfileContext = createContext(null);

const DEFAULT_PROFILE = {
  preferredLanguage: 'English',
  voicePreference: false,
  cognitiveLoadPreference: 'STANDARD', // 'STANDARD' | 'LOW'
  accessibilityNeed: 'NONE', // 'VISUAL' | 'MOTOR' | 'COGNITIVE' | 'LITERACY' | 'NONE'
};

export const ProfileProvider = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [profile, setProfile] = useState(() => {
    const saved = localStorage.getItem('fillforme_profile');
    return saved ? JSON.parse(saved) : DEFAULT_PROFILE;
  });
  const [isLoadingProfile, setIsLoadingProfile] = useState(false);

  useEffect(() => {
    localStorage.setItem('fillforme_profile', JSON.stringify(profile));
  }, [profile]);

  useEffect(() => {
    const fetchProfile = async () => {
      if (isAuthenticated) {
        setIsLoadingProfile(true);
        try {
          const data = await profileApi.getProfile();
          if (data) {
            setProfile(data);
          }
        } catch (err) {
          console.warn('Could not fetch server profile, using local defaults:', err.message);
        } finally {
          setIsLoadingProfile(false);
        }
      }
    };

    fetchProfile();
  }, [isAuthenticated]);

  const updateProfile = async (newProfileData) => {
    const updated = { ...profile, ...newProfileData };
    setProfile(updated);
    if (isAuthenticated) {
      try {
        await profileApi.updateProfile(updated);
      } catch (err) {
        console.error('Failed to sync profile update to backend:', err);
      }
    }
  };

  const toggleCognitiveMode = () => {
    const newMode = profile.cognitiveLoadPreference === 'LOW' ? 'STANDARD' : 'LOW';
    updateProfile({ cognitiveLoadPreference: newMode });
  };

  const toggleVoicePreference = () => {
    updateProfile({ voicePreference: !profile.voicePreference });
  };

  const setLanguage = (lang) => {
    updateProfile({ preferredLanguage: lang });
  };

  const setAccessibilityNeed = (need) => {
    updateProfile({ accessibilityNeed: need });
  };

  return (
    <ProfileContext.Provider
      value={{
        profile,
        isLoadingProfile,
        updateProfile,
        toggleCognitiveMode,
        toggleVoicePreference,
        setLanguage,
        setAccessibilityNeed,
        isCognitiveMode: profile.cognitiveLoadPreference === 'LOW',
      }}
    >
      {children}
    </ProfileContext.Provider>
  );
};

export const useProfile = () => {
  const context = useContext(ProfileContext);
  if (!context) {
    throw new Error('useProfile must be used within a ProfileProvider');
  }
  return context;
};
