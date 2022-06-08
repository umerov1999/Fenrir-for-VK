#include <iostream>
#include <list>
#include <vector>
#include <cstring>
#include <unistd.h>
#include <fcntl.h>
#include "lz4.h"
#include <cstdio>

using namespace std;

#define HEADER "\x02\x4C\x5A\x34"
#pragma pack(push, 1)
struct HDR
{
    char hdr[4];
    int size;
};
#pragma pack(pop)

static string get_content(const string& file) {
    FILE* fl = fopen(file.c_str(), "rb");
	if (fl != NULL)
	{
		string jsonS;
		fseek(fl, 0, SEEK_END);
		jsonS.resize(ftell(fl));
		fseek(fl, 0, SEEK_SET);
		fread((char*)jsonS.data(), 1, jsonS.size(), fl);
		fclose(fl);
		return jsonS;
	}
	cout << "can't open " << file << endl;
	_exit(1);
}

static void write_content(const string& file, const string& data, int size) {
    FILE* fl = fopen(file.c_str(), "wb");
	if (fl != NULL)
	{
		fwrite(data.c_str(), 1, size, fl);
		fclose(fl);
        return;
	}
	cout << "can't open " << file << endl;
	_exit(1);
}

int main(int argc, char* argv[]) {
    locale::global(locale("ru_RU.UTF-8"));
    if (argc < 2) {
        cout << "\ncompress_lz4_lottie - usage is: \n\n      compress_lz4_lottie <source>\n\n";
        return 1;
    }
    string fl = argv[1];
    size_t ps;
    if((ps = fl.find(".json")) != string::npos) {
        string in = get_content(fl);
        unlink(fl.c_str());
        fl = fl.replace(ps, 5, ".lz4");
        std::string out;
        auto cnt = LZ4_compressBound((int) in.size());
        HDR hdr = {};
        memcpy(hdr.hdr, HEADER, strlen(HEADER));
        hdr.size = (int) in.size();

        out.resize(cnt + sizeof(HDR));
        auto size = (uint32_t) LZ4_compress_default(in.data(), ((char *) out.data() + sizeof(HDR)),
                                                (int) in.size(), cnt);
        memcpy((char *) out.data(), &hdr, sizeof(HDR));
        write_content(fl, out, (int)size + sizeof(HDR));
    } else if((ps = fl.find(".lz4")) != string::npos) {
        string in = get_content(fl);
        unlink(fl.c_str());
        fl = fl.replace(ps, 4, ".json");
        HDR hdr = {};
        memcpy(&hdr, in.data(), sizeof(HDR));
        std::string out;
        out.resize(hdr.size);
        LZ4_decompress_safe(((const char *) in.data() + sizeof(HDR)), (char *) out.data(),
                        (int) in.size() - sizeof(HDR), hdr.size);
        write_content(fl, out, (int)out.length());
    } else {
        cout << "\ncompress_lz4_lottie - usage is: \n\n      compress_lz4_lottie <source>\n\n";
        return 1;
    }
    cout << fl << endl;
    cout << "Copyright (c) Umerov Artem, 2022" << endl;
    return 0;
}
