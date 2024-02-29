#include "solver.hpp"
#include <stdexcept>

std::string solve(int level, int height, int width, std::string mapstr) {
    if (height > MAX_SIZE || width > MAX_SIZE) {
        throw std::runtime_error("Map too large");
    }

    // initialize map
    int map[MAX_SIZE][MAX_SIZE] = {0};
    int remaining = 0;
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (mapstr[i * width + j] == 'X') {
                map[i][j] = -1;
            } else {
                map[i][j] = 0;
                remaining++;
            }
        }
    }

    // solve
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            std::pair<int, int> start = {i, j};
            std::string path = singleSolve(height, width, map, remaining, start, "");
            if (path != "") {
                return path;
            }
        }
    }
}

std::string singleSolve(int height, int width, int map[MAX_SIZE][MAX_SIZE], int remaining, std::pair<int, int> cur, std::string path) {
    if (remaining == 0) {
        return path;
    }

    for (auto& [dir, d] : directions) {
        std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
        if (0 <= next.first && next.first < height && 0 <= next.second && next.second < width && map[next.first][next.second] == 0) {
            path += directionChars[dir];
            while (0 <= next.first && next.first < height && 0 <= next.second && next.second < width && map[next.first][next.second] == 0) {
                map[next.first][next.second] = 1;
                remaining--;
                
                next.first += d.first;
                next.second += d.second;
            }
            cur = {next.first - d.first, next.second - d.second};
            if (remaining == 0) {
                return path;
            }
            std::string result = singleSolve(height, width, map, remaining, cur, path);
            if (result != "") {
                return result;
            }
        }
    }

    return "";
}

