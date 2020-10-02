/*
This program count the maximum number of ways a name can be encrypted if 
a=1
.
.
.
.
z=26
A=27
.
.
.
.
Z=52
*/

#include <iostream>

using namespace std;

int main(){
	int num,n,t=0,i,now=1;
	cin>>num;
	int temp = num;
	int a[10];
	//i=0;
	while(num!=0)
	{
		t=t+1;
		n=num%10;
		//a[i]=n;
		//i=i+1;
		num=num/10;
	}
	for(i=0;i<t;i++){
		n=temp%10;
		a[i]=n;
	}
	for(i=t-1;i>0;i--)
	{
		int val;
		val = (a[i-1]*10)+(a[i]);
		if(val<=52)
		{
			now=now+1;
		}
	}
	cout << now;
}
