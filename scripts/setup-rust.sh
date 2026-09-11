#!/bin/bash
set -e

PROJECT_DIR=$(cd "$(dirname "$0")/.." && pwd)
RUST_VERSION=$("$PROJECT_DIR/scripts/read-property.sh" \
  "$PROJECT_DIR/version.properties" version.rust)
RUST_DIR="$PROJECT_DIR/.gradle/tgx-rust"
export CARGO_HOME="$RUST_DIR/cargo"
export RUSTUP_HOME="$RUST_DIR/rustup"

if [[ ! -x "$CARGO_HOME/bin/rustup" ]]; then
  mkdir -p "$RUST_DIR"
  INSTALLER="$RUST_DIR/rustup-init.sh"
  trap 'rm -f "$INSTALLER"' EXIT
  wget -q -O "$INSTALLER" https://sh.rustup.rs
  sh "$INSTALLER" -y --no-modify-path --profile minimal --default-toolchain none
fi

"$CARGO_HOME/bin/rustup" toolchain install "$RUST_VERSION" --profile minimal
TARGETS=(aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android)
for TARGET in "${TARGETS[@]}"; do
  "$CARGO_HOME/bin/rustup" target add --toolchain "$RUST_VERSION" "$TARGET"
done

echo "Rust $RUST_VERSION is ready in $RUST_DIR"
