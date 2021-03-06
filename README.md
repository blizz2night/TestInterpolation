# TestInterpolation
研究GLES纹理采样时线性过滤对采样结果的影响

我们创建了一个 [白黑白黑] 4*1像素的纹理
如图  
![render](https://github.com/blizz2night/TestInterpolation/blob/master/1592155165687.png)
我们考察$X$轴, 记归一化后坐标为$x_{norm}\in[0,1]$, 归一化前$x\in[0,4]$
考察第二个黑色像素点,
1. 纹理过滤类型为最近邻(*NEAREST*)时,
$x\in[1,2],即x_{norm}\in[0.25,0.5]$, 采样颜色是黑色
2. 对于线性(*LINEAR*)纹理
如果采用线性过滤,仅当$x=1.5$,即$x_{norm}=1.5/4 = 0.375$, 采样出的颜色是黑色,  
在相邻坐标的采样都会因为硬件插值导致颜色和白色像素混合
