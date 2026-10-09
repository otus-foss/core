"""Release checks reject missing platform variants and damaged Maven artifacts."""

import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_publications import TARGETS, violations


class PublicationTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.repository = Path(self.temporary.name)
        for suffix, extension in TARGETS.items():
            artifact = "sample" + suffix
            directory = self.repository / "com" / "otus" / artifact / "1.0.0"
            directory.mkdir(parents=True)
            basename = f"{artifact}-1.0.0"
            content = b"compiled library"
            (directory / f"{basename}.{extension}").write_bytes(content)
            (directory / f"{basename}.pom").write_text("<project />")
            metadata = {
                "component": {"group": "com.otus", "module": "sample", "version": "1.0.0"},
                "variants": [
                    {
                        "files": [
                            {
                                "url": f"{basename}.{extension}",
                                "size": len(content),
                                "sha256": hashlib.sha256(content).hexdigest(),
                            }
                        ]
                    }
                ],
            }
            if not suffix:
                metadata["variants"].extend(
                    {
                        "available-at": {
                            "group": "com.otus",
                            "module": "sample" + target,
                            "version": "1.0.0",
                        }
                    }
                    for target in TARGETS
                    if target
                )
            (directory / f"{basename}.module").write_text(json.dumps(metadata))

    def artifact_path(self, suffix, extension):
        artifact = "sample" + suffix
        return (
            self.repository / "com" / "otus" / artifact / "1.0.0" / f"{artifact}-1.0.0.{extension}"
        )

    def check(self):
        return violations(self.repository, ["sample"], "1.0.0")

    def test_complete_release_passes(self):
        self.assertEqual([], self.check())

    def test_missing_ios_binary_fails(self):
        for suffix in ("-iosarm64", "-iossimulatorarm64"):
            with self.subTest(suffix=suffix):
                path = self.artifact_path(suffix, "klib")
                content = path.read_bytes()
                path.unlink()
                self.assertTrue(any("Missing" in error for error in self.check()))
                path.write_bytes(content)

    def test_damaged_android_binary_fails(self):
        self.artifact_path("-android", "aar").write_bytes(b"wrong library!!!")
        self.assertTrue(any("does not match metadata" in error for error in self.check()))

    def test_missing_platform_reference_fails(self):
        path = self.artifact_path("", "module")
        metadata = json.loads(path.read_text())
        metadata["variants"].pop()
        path.write_text(json.dumps(metadata))
        self.assertTrue(any("Incomplete platform references" in error for error in self.check()))

    def test_invalid_metadata_fails(self):
        self.artifact_path("-iosarm64", "module").write_text("{broken")
        self.assertTrue(any("Invalid publication metadata" in error for error in self.check()))

    def test_wrong_version_fails(self):
        path = self.artifact_path("-android", "module")
        metadata = json.loads(path.read_text())
        metadata["component"]["version"] = "0.9.0"
        path.write_text(json.dumps(metadata))
        self.assertTrue(any("Wrong publication coordinates" in error for error in self.check()))


if __name__ == "__main__":
    unittest.main()
