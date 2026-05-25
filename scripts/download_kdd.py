import gzip
import shutil
from pathlib import Path

import kagglehub


def main() -> None:
    path = Path(kagglehub.dataset_download("galaxyh/kdd-cup-1999-data"))
    print("Path to dataset files:", path)

    gz_candidates = list(path.rglob("kddcup.data_10_percent.gz"))
    if not gz_candidates:
        print("kddcup.data_10_percent.gz was not found in the downloaded dataset.")
        return

    output_dir = Path("data")
    output_dir.mkdir(exist_ok=True)
    output_path = output_dir / "kddcup.data_10_percent.csv"
    with gzip.open(gz_candidates[0], "rb") as source, output_path.open("wb") as target:
        shutil.copyfileobj(source, target)

    print("Extracted CSV:", output_path)


if __name__ == "__main__":
    main()
