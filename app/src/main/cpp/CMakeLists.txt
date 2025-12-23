cmake_minimum_required(VERSION 3.22.1)

project("smartinstrument" VERSION 1.0.0 LANGUAGES CXX)

# Trova il pacchetto Oboe (prefab)
find_package(oboe REQUIRED CONFIG)

# Aggiungi la libreria nativa
add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    AudioEngine.cpp
    Oscillator.cpp
    ADSREnvelope.cpp
)

# Imposta le proprietà C++
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)

# Linka le librerie necessarie
target_link_libraries(${CMAKE_PROJECT_NAME}
    oboe::oboe
    android
    log
)

# Ottimizzazioni per bassa latenza
target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE
    -Wall
    -Werror
    -O3
    -ffast-math
)
