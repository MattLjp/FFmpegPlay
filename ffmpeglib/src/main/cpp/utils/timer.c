//
// Created by liaojp on 2023/1/6.
//

#include <sys/time.h>

int64_t GetSysCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    int64_t ts = (int64_t)tv.tv_sec * 1000 + tv.tv_usec / 1000;
    return ts;
}