package krasa.wakeonlan.ssh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdaterTest {

	@Test
	void isNewer() {
		assertTrue(Updater.isNewer("1.12.1", "1.13.5"));
		assertTrue(Updater.isNewer("1.12.1", "1.13"));
		assertTrue(Updater.isNewer("1.12.1", "2"));
		assertTrue(Updater.isNewer("1.12.1", "2.13.5"));


		assertFalse(Updater.isNewer("2", "1"));
		assertFalse(Updater.isNewer("1.12.1", "1.12.1"));
	}
}