import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { arusRadii } from '@/design/tokens';

export default function HomeScreen() {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <ThemedView style={styles.heroSection}>
          <ThemedText type="title" style={styles.title}>
            Arus
          </ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.subtitle}>
            Fondasi keuangan pribadi yang offline-first.
          </ThemedText>
        </ThemedView>

        <ThemedView type="backgroundElement" style={styles.foundationCard}>
          <ThemedText type="smallBold">Fondasi siap digunakan</ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.cardMessage}>
            Penyimpanan lokal, tema, dan batas domain sudah tersambung. Fitur keuangan akan hadir di alur berikutnya.
          </ThemedText>
        </ThemedView>
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    flexDirection: 'row',
  },
  safeArea: {
    flex: 1,
    paddingHorizontal: Spacing.four,
    alignItems: 'center',
    gap: Spacing.three,
    paddingBottom: BottomTabInset + Spacing.three,
    maxWidth: MaxContentWidth,
  },
  heroSection: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
    paddingHorizontal: Spacing.four,
    gap: Spacing.four,
  },
  title: {
    textAlign: 'center',
  },
  subtitle: {
    textAlign: 'center',
    marginTop: Spacing.two,
  },
  foundationCard: {
    alignSelf: 'stretch',
    paddingHorizontal: Spacing.section,
    paddingVertical: Spacing.section,
    borderRadius: arusRadii.card,
  },
  cardMessage: {
    marginTop: Spacing.two,
  },
});
