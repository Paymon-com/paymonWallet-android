#include <locale>
#include "Utils.h"

std::vector<std::string> Utils::splitString(const std::string& str, char separator) {
    std::vector<std::string> arr;
    std::stringstream ss(str);
    std::string segment;

    while(std::getline(ss, segment, separator)) {
        arr.push_back(segment);
    }
    return arr;
}


