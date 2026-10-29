import { SQLiteProvider } from 'expo-sqlite';
import { useCallback, useState, type PropsWithChildren } from 'react';
import { Pressable, SafeAreaView, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { UnsupportedDatabaseVersionError, initializeDatabase } from '@/db/migrations';

const DATABASE_NAME = 'arus.db';

type InitializationState = 'loading' | 'ready' | 'error';

type DisplayError = {
  title: string;
  message: string;
};

function toDisplayError(error: unknown): DisplayError {
  if (error instanceof UnsupportedDatabaseVersionError) {
    return {
      title: 'Arus perlu diperbarui',
      message: 'Data lokal dibuat oleh versi Arus yang lebih baru. Perbarui aplikasi untuk melanjutkan tanpa mengubah data Anda.',
    };
  }
  return {
    title: 'Penyimpanan lokal belum siap',
    message: 'Arus tidak dapat membuka penyimpanan lokal. Coba lagi; data yang ada tidak dihapus.',
  };
}

function DatabaseLoadingBoundary() {
  return (
    <ThemedView style={styles.boundary}>
      <SafeAreaView style={styles.boundaryContent}>
        <ThemedText type="title">Menyiapkan Arus</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.boundaryMessage}>
          Menyiapkan penyimpanan lokal untuk penggunaan offline.
        </ThemedText>
      </SafeAreaView>
    </ThemedView>
  );
}

function DatabaseErrorBoundary({ error, onRetry }: { error: DisplayError; onRetry: () => void }) {
  const theme = useTheme();

  return (
    <ThemedView style={styles.boundary}>
      <SafeAreaView style={styles.boundaryContent}>
        <ThemedText type="title">{error.title}</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.boundaryMessage}>
          {error.message}
        </ThemedText>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Coba buka penyimpanan lokal lagi"
          onPress={onRetry}
          style={({ pressed }) => [styles.retryButton, { backgroundColor: theme.primary }, pressed && styles.pressed]}>
          <ThemedText style={[styles.retryLabel, { color: theme.onPrimary }]}>Coba lagi</ThemedText>
        </Pressable>
      </SafeAreaView>
    </ThemedView>
  );
}

export function DatabaseProvider({ children }: PropsWithChildren) {
  const [attempt, setAttempt] = useState(0);
  const [state, setState] = useState<InitializationState>('loading');
  const [displayError, setDisplayError] = useState<DisplayError | null>(null);

  const onInit = useCallback(async (db: Parameters<typeof initializeDatabase>[0]) => {
    setState('loading');
    setDisplayError(null);
    await initializeDatabase(db);
    setState('ready');
  }, []);

  const onError = useCallback((error: Error) => {
    // expo-sqlite invokes onError while rendering its non-suspense provider.
    // Defer our boundary update so we do not update this parent during that
    // child render.
    setTimeout(() => {
      setState('error');
      setDisplayError(toDisplayError(error));
    }, 0);
  }, []);

  const retry = useCallback(() => {
    setDisplayError(null);
    setState('loading');
    setAttempt((current) => current + 1);
  }, []);

  if (state === 'error' && displayError !== null) {
    return <DatabaseErrorBoundary error={displayError} onRetry={retry} />;
  }

  return (
    <View style={styles.providerContainer}>
      {state === 'loading' && <DatabaseLoadingBoundary />}
      <SQLiteProvider
        key={attempt}
        databaseName={DATABASE_NAME}
        onInit={onInit}
        onError={onError}>
        {children}
      </SQLiteProvider>
    </View>
  );
}

const styles = StyleSheet.create({
  providerContainer: {
    flex: 1,
  },
  boundary: {
    ...StyleSheet.absoluteFill,
    zIndex: 10,
  },
  boundaryContent: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: Spacing.screen,
  },
  boundaryMessage: {
    maxWidth: 360,
    marginTop: Spacing.two,
    textAlign: 'center',
  },
  retryButton: {
    minHeight: Spacing.touchTarget,
    marginTop: Spacing.section,
    paddingHorizontal: Spacing.section,
    paddingVertical: Spacing.two,
    borderRadius: Spacing.three,
    justifyContent: 'center',
  },
  retryLabel: {},
  pressed: {
    opacity: 0.75,
  },
});
