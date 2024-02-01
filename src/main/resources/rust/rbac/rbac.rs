#![allow(dead_code)]

use std::mem;
use std::slice;

extern "C" {
  fn evaluation_grant();
  fn evaluation_deny();

  fn identity_has_realm_role(ptr: *const u8, len: i32) -> bool;
}

const REALM_ROLE : &str = "offline_access";

#[cfg_attr(all(target_arch = "wasm32"), export_name = "evaluate")]
#[no_mangle]
pub unsafe extern fn evaluate() {
  let len = REALM_ROLE.chars().count() as i32;
  let ptr = alloc(len as i32);
  let bytes = unsafe { slice::from_raw_parts_mut(ptr as *mut u8, len as usize) };

  let mut idx: i32 = 0;
  for ch in REALM_ROLE.bytes() {
      bytes[idx as usize] = ch;
      idx += 1;
  }

  if identity_has_realm_role(ptr, len as i32) {
    evaluation_grant();
  } else {
    evaluation_deny();
  }

  dealloc(ptr as *mut u8, len);
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
