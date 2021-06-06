#include <string>
#include <array>
using namespace std;

extern "C" __declspec(dllexport) double Add(double a, double b)
{
    return a + b;
}



uint32_t rotateLeft(int y, int c)
{
  return ((y << c) | (y >> (32 - c)));
}

extern "C" __declspec(dllexport) void xorSalsa8(int* X, int xi, int di)
{
    int i = 0;

    int x00 = (X[di] ^= X[xi + 0]);
    int x01 = (X[di + 1] ^= X[xi + 1]);
    int x02 = (X[di + 2] ^= X[xi + 2]);
    int x03 = (X[di + 3] ^= X[xi + 3]);
    int x04 = (X[di + 4] ^= X[xi + 4]);
    int x05 = (X[di + 5] ^= X[xi + 5]);
    int x06 = (X[di + 6] ^= X[xi + 6]);
    int x07 = (X[di + 7] ^= X[xi + 7]);
    int x08 = (X[di + 8] ^= X[xi + 8]);
    int x09 = (X[di + 9] ^= X[xi + 9]);
    int x10 = (X[di + 10] ^= X[xi + 10]);
    int x11 = (X[di + 11] ^= X[xi + 11]);
    int x12 = (X[di + 12] ^= X[xi + 12]);
    int x13 = (X[di + 13] ^= X[xi + 13]);
    int x14 = (X[di + 14] ^= X[xi + 14]);
    int x15 = (X[di + 15] ^= X[xi + 15]);

    for (i = 0; i < 8; i += 2)
    {
        x04 ^= rotateLeft(x00 + x12, 7);
        x08 ^= rotateLeft(x04 + x00, 9);
        x12 ^= rotateLeft(x08 + x04, 13);
        x00 ^= rotateLeft(x12 + x08, 18);
        x09 ^= rotateLeft(x05 + x01, 7);
        x13 ^= rotateLeft(x09 + x05, 9);
        x01 ^= rotateLeft(x13 + x09, 13);
        x05 ^= rotateLeft(x01 + x13, 18);
        x14 ^= rotateLeft(x10 + x06, 7);
        x02 ^= rotateLeft(x14 + x10, 9);
        x06 ^= rotateLeft(x02 + x14, 13);
        x10 ^= rotateLeft(x06 + x02, 18);
        x03 ^= rotateLeft(x15 + x11, 7);
        x07 ^= rotateLeft(x03 + x15, 9);
        x11 ^= rotateLeft(x07 + x03, 13);
        x15 ^= rotateLeft(x11 + x07, 18);
        x01 ^= rotateLeft(x00 + x03, 7);
        x02 ^= rotateLeft(x01 + x00, 9);
        x03 ^= rotateLeft(x02 + x01, 13);
        x00 ^= rotateLeft(x03 + x02, 18);
        x06 ^= rotateLeft(x05 + x04, 7);
        x07 ^= rotateLeft(x06 + x05, 9);
        x04 ^= rotateLeft(x07 + x06, 13);
        x05 ^= rotateLeft(x04 + x07, 18);
        x11 ^= rotateLeft(x10 + x09, 7);
        x08 ^= rotateLeft(x11 + x10, 9);
        x09 ^= rotateLeft(x08 + x11, 13);
        x10 ^= rotateLeft(x09 + x08, 18);
        x12 ^= rotateLeft(x15 + x14, 7);
        x13 ^= rotateLeft(x12 + x15, 9);
        x14 ^= rotateLeft(x13 + x12, 13);
        x15 ^= rotateLeft(x14 + x13, 18);
    }

    X[di] += x00;
    X[di + 1] += x01;
    X[di + 2] += x02;
    X[di + 3] += x03;
    X[di + 4] += x04;
    X[di + 5] += x05;
    X[di + 6] += x06;
    X[di + 7] += x07;
    X[di + 8] += x08;
    X[di + 9] += x09;
    X[di + 10] += x10;
    X[di + 11] += x11;
    X[di + 12] += x12;
    X[di + 13] += x13;
    X[di + 14] += x14;
    X[di + 15] += x15;


}




