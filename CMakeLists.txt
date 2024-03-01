cmake_minimum_required(VERSION 3.20)

set(CMAKE_CXX_STANDARD 20)

project(solver)

add_library(solver SHARED solver.cpp)

add_executable(test test.cpp solver.cpp)