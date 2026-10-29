import { ScrollView, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { arusRadii } from '@/design/tokens';

export default function FoundationScreen() {
  const safeAreaInsets = useSafeAreaInsets();

  return (
    <ScrollView
      style={styles.scrollView}
      contentContainerStyle={{
        paddingTop: safeAreaInsets.top + Spacing.section,
        paddingBottom: safeAreaInsets.bottom + BottomTabInset + Spacing.section,
      }}>
      <ThemedView style={styles.container}>
        <ThemedText type="title">Fondasi Arus</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.intro}>
          Batas yang menjaga setiap fitur tetap konsisten, aman, dan dapat digunakan tanpa jaringan.
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.card}>
          <ThemedText type="smallBold">Lokal terlebih dahulu</ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.cardText}>
            Data aplikasi disiapkan untuk tersimpan di SQLite lokal. Tidak ada permintaan jaringan pada shell ini.
          </ThemedText>
        </ThemedView>

        <ThemedView type="backgroundElement" style={styles.card}>
          <ThemedText type="smallBold">Batas yang jelas</ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.cardText}>
            Route hanya merangkai layar dan provider. Aturan domain serta persistence berada di lapisan masing-masing.
          </ThemedText>
        </ThemedView>

        <ThemedView type="backgroundElement" style={styles.card}>
          <ThemedText type="smallBold">Siap diverifikasi</ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.cardText}>
            Jalankan lint, typecheck, dan doctor sebelum setiap perubahan foundation diteruskan ke fitur berikutnya.
          </ThemedText>
        </ThemedView>
      </ThemedView>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollView: {
    flex: 1,
  },
  container: {
    width: '100%',
    maxWidth: MaxContentWidth,
    alignSelf: 'center',
    paddingHorizontal: Spacing.screen,
  },
  intro: {
    marginTop: Spacing.two,
    marginBottom: Spacing.section,
  },
  card: {
    paddingHorizontal: Spacing.section,
    paddingVertical: Spacing.section,
    borderRadius: arusRadii.card,
    marginBottom: Spacing.two,
  },
  cardText: {
    marginTop: Spacing.two,
  },
});
