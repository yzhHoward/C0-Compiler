int x=7;

char fun(int num) {
    int rtv = num/2;
    return rtv+1;
}

void main() {
    char m = fun(x);
    const int e12=1;
    fun(x);
    x = 7;
    while(x==7) {
        int m = 5;
        print(m);
    }
    print(5);
    return;
}