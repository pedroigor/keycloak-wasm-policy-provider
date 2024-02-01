#! /bin/bash
set -euxo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# rustup target add wasm32-unknown-unknown

cargo build --target wasm32-unknown-unknown --release

rm -f $SCRIPT_DIR/../resources/wasm/abac.wasm
cp $SCRIPT_DIR/target/wasm32-unknown-unknown/release/abac.wasm $SCRIPT_DIR/../../wasm
