# JavaNIO

Buffer 类

一个Buffer对象是固定数量的数据的容器. 对于每一个非布尔原始数据类型都有一个缓冲区类.

缓冲区的工作与通道紧密联系. 通道是IO传输发生时通过的入口,而缓冲区是这些数据传世的来源或目标.

对于离开缓冲区的传输,想传递出去的数据被置于一个缓冲区,被传送到通道.

对于传回缓冲区的传输,一个通道将数据放置在我们提供的缓冲区中.

这种在协同对象之间进行的缓冲区数据传递是高效数据处理的关键.

Buffer类的家谱

![Buffer](https://note.youdao.com/yws/api/personal/file/6F5DBD25D42141AD8BBECB1750D651FD?method=download&shareKey=dd567ae41851bdce0d8a707f7b5dc052)

尽管缓冲区作用于它们存储的原始数据类型,但缓冲区十分倾向于处理字节.

缓冲区基础:

缓冲区是包在一个对象内的基本数据元素数组. Buffer类相比简单数组的优点是它将数据的数据内容和信息包含在一个单一的对象中. Buffer类及其专有的子类定义了一个用户处理数据缓冲区的API

1.1. 属性

所有缓冲区都具有四个属性来提供关于其所包含的数据元素的信息.

+ 容量(Capacity)

+ 上界(Limit)

+ 位置(Position)

+ 标记(Mark)

0 <= mark <= position <= limit <= capacity

1.2. Buffer API :

![Buffer-Method](https://note.youdao.com/yws/api/personal/file/61F241C0339D434F847C773950B540ED?method=download&shareKey=ffdad5554cd7414d1a725aa233b21f07)

所有缓冲区都是可读的, isReadOnly()用于区分缓冲区是否可修改.

1.3. 存取

调用put() 进行存储, put() 被调用时指出下一个数据元素应该被插入的位置

调用get() 进行取出, get() 被调用时指出下一个元素应从何处检索

但是在 1.2 的API图中并没有包括put() get() , 因为每一个Buffer类都有这两个函数, 但是它们采用的参数类型, 以及返回的数据类型 对于每个子类来说都是唯一的, 所以它们不能放在顶层Buffer类中抽象.

以ByteBuffer为例, 下面是新建的容量为10的ByteBuffer逻辑视图.

![newBuffer](https://note.youdao.com/yws/api/personal/file/D79C7A8B08C542088C91A8AA56521258?method=download&shareKey=6ee718f10532813348649eefd7354996)

对上面的Buffer调用 5 次`put()` 后,缓冲区状态如下 

`buffer.put((byte)'H').put((byte)'e').put((byte)'l').put((byte)'l').put((byte)'0');`

![5 put Buffer](https://note.youdao.com/yws/api/personal/file/7721FE622AD64400B5564B4C1FCE36BE?method=download&shareKey=ac6c1253c2e50730eea59af7575728d3)

Buffer API 支持级联调用

```java
buffer.mark();
buffer.position(5);
buffer.reset();

// 以上可以简写为
Buffer.mark().position(5).reset();
```

如果自己也希望实现可以级联的操作, 那在函数中返回`this`就行了

![return this](https://note.youdao.com/yws/api/personal/file/8D411CCC3B754C3285A3B7FD0DEBC0A9?method=download&shareKey=7a51335a686e775360863e04f2c3eedc)

1.4. 翻转

把缓冲区传递给通道, 通道在缓冲区上执行get()操作, 缓冲区中从position到limit的数据将被取出. 

limit 指明了缓冲区有效内容的末端. 

将limit设置为当前位置, 然后将position设置为0, 就可以实现对已读数据的重新获取,也称为翻转:

```java
buffer.limit(buffer.position()).position(0)
```

Buffer中的flip() 将上述需求进行了实现,

```java
Buffer.flip()
```

`Flip()` 函数讲一个能够继续添加数据元素的填充状态的缓冲区翻转成一个准备读取元素的释放状态.

`buffer.position(5).limit(10)` 调用1次flip()和调用2次flip() 后的状态如下:

![flip](https://note.youdao.com/yws/api/personal/file/605633556A424F81B13515358FE5A0E6?method=download&shareKey=05e53adba475165c7d2d171144a01771)

`Rewind()` 函数与`Flip()` 相似, 但不影响 limit 属性, 它只是将position设置为0.

`buffer.position(5).limit(10)` 调用 rewind() 后的状态如下:

![rewind](https://note.youdao.com/yws/api/personal/file/E38A8BE6BB954890AAD4D751B0C42F1E?method=download&shareKey=3362912adb02138092de1e856502ffb3)

1.5. 释放

如果一个通道(channel)`read()`操作完成,而我们想查看被通道放入缓冲区内的数据,那么我们需要在调用`get()`之前翻转缓冲区.

channel对象在buffer对象上调用`put()`增加数据; `put`和`read` 可以随意混合使用.

`hasRemaining()` 会在释放缓冲区时检查缓冲区是否达到limit值.

```java
public final boolean hasRemaining() {
    return position < limit;
}
```

`remaining()` 返回当前位置到limit的差值, 即剩余多少个元素

```java
public final int remaining() {
    return limit - position;
}
```

释放缓冲区Example 1 :

```java
for(int i=0; buffer.hasRemaining(), i++){
    myByteArray[i] = buffer.get()
}
```

释放缓冲区Example 2 :

```java
int count = buffer.remainning();
for(int i=0; i<count; i++){
    myByteArray[i] = buffer.get();
}
```

明显第二个方法会更高效,因为它不会在每次循环时重复检查buffer的可用元素.

但是第一个方法允许在多线程环境下同时从缓冲区释放元素.

`clear()` 将缓冲区重置为空状态. 它并不改变缓冲区中的任何数据元素, 而是仅仅将limit设置为capacity的值,并把position设置为0.

```java
public final Buffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;
}
```

释放缓冲区Example 3 :

```java

import org.junit.Test;

import java.nio.CharBuffer;

public class CustomTest {
    private static int index = 0;
    private static String[] str = {"hello","world"};
    public static boolean fillBuffer(CharBuffer buffer){
        if(index>=str.length){
            return false;
        }
        String item = str[index++];
        for (int i = 0; i < item.length(); i++) {
            buffer.put(item.charAt(i));
        }
        return true;
    }

    public static void releaseBuffer(CharBuffer buffer){
        while (buffer.hasRemaining()){
            System.out.print(buffer.get());
        }
        System.out.println("");
    }

    @Test
    public void testReleaseBuffer(){
        CharBuffer buffer = CharBuffer.allocate(100);

        while (fillBuffer(buffer)){
            buffer.flip();
            releaseBuffer(buffer);
            buffer.clear();
        }
    }
}

```

1.6. 压缩

从缓冲区中释放一部分数据,但不是全部,然后重新填充. 为了实现这一点, 未读的数据元素需要下移,以使第一个元素索引为0.

简单来说, 压缩就是丢弃已经释放的数据, 保留未释放的数据,并使缓冲区对重新填充容量准备就绪.

`compact()` 函数可以做到这一点. 其在复制数据时要比用`get()`和`put()`函数高效的多.

```java
public ByteBuffer compact() {

    int pos = position();
    int lim = limit();
    assert (pos <= lim);
    int rem = (pos <= lim ? lim - pos : 0);

    unsafe.copyMemory(ix(pos), ix(0), (long)rem << 0);
    position(rem);
    limit(capacity());
    discardMark();
    return this;
}
```

通过两幅图来解释`compact()`的代码含义

![compact](https://note.youdao.com/yws/api/personal/file/22275D11B2594A42AE01169563ACA248?method=download&shareKey=9518f1bd02a98ce1a25964bf35cf828d)

说明: 
    1. 数据元素 2-4 被复制到 0-2 位置.
    2. 位置3-4不受影响, 但是超出了position的位置, 因此可以被之后的put()调用重写.
    3. position(rem) , limit(capacity())
    
1.7. 标记

使缓冲区能够记住一个位置,并在之后将其返回.

缓冲区的标记在 `mark()` 函数被调用之前是未定义的. 调用时,标记被设置为position的值.

`reset()` 会将position的值设置为mark的值.

1.8. 比较

equals

```java

public boolean equals(Object ob) {
    if (this == ob)
        return true;
    if (!(ob instanceof ByteBuffer))
        return false;
    ByteBuffer that = (ByteBuffer)ob;
    if (this.remaining() != that.remaining())
        return false;
    int p = this.position();
    for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--)
        if (!equals(this.get(i), that.get(j)))
            return false;
    return true;
}
private static boolean equals(byte x, byte y) {

    return x == y;

}
```

compareTo

```java
public int compareTo(ByteBuffer that) {
    int n = this.position() + Math.min(this.remaining(), that.remaining());
    for (int i = this.position(), j = that.position(); i < n; i++, j++) {
        int cmp = compare(this.get(i), that.get(j));
        if (cmp != 0)
            return cmp;
    }
    return this.remaining() - that.remaining();
}

private static int compare(byte x, byte y) {

    return Byte.compare(x, y);

}

```

1.9. 批量移动

一次移动一个数据元素效率并不高,缓冲区的目的是为了能够高效传输数据.

```java
public abstract class CharBuffer extends Buffer
    implements Comparable<CharBuffer>, Appendable, CharSequence, Readable{
    
    public CharBuffer get(char[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }
    
    public CharBuffer get(char[] dst) {
        return get(dst, 0, dst.length);
    }
    
    public CharBuffer put(CharBuffer src) {
        if (src == this)
            throw new IllegalArgumentException();
        if (isReadOnly())
            throw new ReadOnlyBufferException();
        int n = src.remaining();
        if (n > remaining())
            throw new BufferOverflowException();
        for (int i = 0; i < n; i++)
            put(src.get());
        return this;
    }
    
    public CharBuffer put(char[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            this.put(src[i]);
        return this;
    }
    
    public final CharBuffer put(char[] src) {
        return put(src, 0, src.length);
    }
    
    public CharBuffer put(String src, int start, int end) {
        checkBounds(start, end - start, src.length());
        if (isReadOnly())
            throw new ReadOnlyBufferException();
        if (end - start > remaining())
            throw new BufferOverflowException();
        for (int i = start; i < end; i++)
            this.put(src.charAt(i));
        return this;
    }
    
    public final CharBuffer put(String src) {
        return put(src, 0, src.length());
    }

}
```
