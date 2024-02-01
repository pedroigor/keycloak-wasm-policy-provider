#![allow(dead_code)]

use std::mem;
use std::slice;

extern "C" {
  fn evaluation_grant();
  fn evaluation_deny();

  fn identity_has_attribute_value(key_ptr: *const u8, key_len: i32, value_ptr: *const u8, value_len: i32) -> bool;
}

const ATTRIBUTE_KEY : &str = "email";
const ATTRIBUTE_VALUE : &str = "alice@keycloak.com";

#[cfg_attr(all(target_arch = "wasm32"), export_name = "evaluate")]
#[no_mangle]
pub unsafe extern fn evaluate() {
  let key_len = ATTRIBUTE_KEY.chars().count() as i32;
  let value_len = ATTRIBUTE_VALUE.chars().count() as i32;
  let key_ptr = alloc(key_len as i32);
  let value_ptr = alloc(key_len as i32);
  let key_bytes = unsafe { slice::from_raw_parts_mut(key_ptr as *mut u8, key_len as usize) };
  let value_bytes = unsafe { slice::from_raw_parts_mut(value_ptr as *mut u8, value_len as usize) };

  let mut idx: i32 = 0;
  for ch in ATTRIBUTE_KEY.bytes() {
      key_bytes[idx as usize] = ch;
      idx += 1;
  }
  idx = 0;
  for ch in ATTRIBUTE_VALUE.bytes() {
    value_bytes[idx as usize] = ch;
    idx += 1;
  }

  if identity_has_attribute_value(key_ptr, key_len as i32, value_ptr, value_len as i32) {
    evaluation_grant();
  } else {
    evaluation_deny();
  }

  dealloc(key_ptr as *mut u8, key_len);
  dealloc(value_ptr as *mut u8, value_len);
}

#[cfg_attr(all(target_arch = "wasm32"), export_name = "alloc")]
#[no_mangle]
pub unsafe extern "C" fn alloc(len: i32) -> *const u8 {
    let mut buf = Vec::with_capacity(len as usize);
    let ptr = buf.as_mut_ptr();
    // tell Rust not to clean this up
    mem::forget(buf);
    ptr
}

#[cfg_attr(all(target_arch = "wasm32"), export_name = "dealloc")]
#[no_mangle]
pub unsafe extern "C" fn dealloc(ptr: *mut u8, len: i32) {
    let _ = Vec::from_raw_parts(ptr, 0, len as usize);
}
