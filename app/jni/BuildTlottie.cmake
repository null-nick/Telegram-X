# tlottie

set(TLOTTIE_SOURCE_DIR "${THIRDPARTY_DIR}/tlottie")
set(TLOTTIE_LIB_PATH "${TLOTTIE_DIR}/${ANDROID_ABI}/lib/libtlottie.a")

add_library(tlottie STATIC IMPORTED)
set_target_properties(tlottie PROPERTIES IMPORTED_LOCATION "${TLOTTIE_LIB_PATH}")
target_include_directories(tlottie INTERFACE "${TLOTTIE_SOURCE_DIR}/include")

if(NOT EXISTS "${TLOTTIE_LIB_PATH}")
  message(WARNING "Missing dependency: tlottie. Run :app:buildTlottie or Refresh Linked C++ Projects")
endif()
