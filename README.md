# KotlinUtils

### Commons

 +  SharedPreferences
 +  Async
 +  Toast 
 +  SimpleEventBus
 +  Interval
 +  Event 
 +  Throttle

##### sharedPreferences

```kotlin
//save
sharedPreferences {
    putBoolean(k,v)
    putString(k,v)
    putInt(k,v)
    putLong(k,v)
    putSerializable(k,v)
}.commit()

//get
sharedPreferences {
    getBoolean(k)
    getString(k)
    getInt(k)
    getLong(k)
    getSerializable(k)
}

//infix 
//支持 String,Int,Long,Boolean,Serializable
value prefs key
//例如
"zhangsan" prefs "username"
...

//开启，在Application中使用
enableSharedPreferences("prefs name")
```

##### async

```kotlin
//异步
doAsync { ... }
doAsync { //使用主线程
    uiThread { ... }
}

T.doAsync { ... }
```

##### toast

```kotlin
在Context下使用
//Toast.LENGHT_SHORT
toast("")
//Toast.LENGTH_LONG
toastLong("")
```

####  simpleEventbus

```kotlin
//订阅 支持infix写法
//订阅里只能订阅一种类型，相同类型暂时不支持，如果有相同类型则会覆盖上一个
subscriptions {
    //订阅指定类型
    subscribe<Type>().observe { ... }
    //订阅粘性事件
    subscribe<Type>().sticky { ... }
    //线程操作
    //Schedulers.ui / async / immediate
    subscribe<Type>(Schedulers.ui).observe { ... }
}

eg :
subscriptions {
    subscribe<UserInfo>().observe { //TODO }
    subscribe<ModifyEvent>().sticky { //TODO }
    subscribe<JumpEvent>(Schedulers.async).observe { //TODO }
}


value.post() //发送普通事件
value.postSticky() //发送粘性事件

eg : 
val info = UserInfo().apply {
    username = "user"
    age = 11
}
info.post()
info.postSticky()

info.doAsync {
    post()
    ...
}

unsubscribe() //取消订阅 必须要加

```

#### view

```kotlin
//点击事件
view.clicks { ... }
//文本改变
editText.textChanges { ... }
```

#### interval

```kotlin
T.interval(10f) { //整数为秒 10f为10s 0.5f为0.5s也就是500ms
    //开始时
    start { ... }
    //完成时
    completed { ... }
    //计时时间回调（倒数）
    next { ... }
}
```

#### throttle

```kotlin
//节流器
//使用在view事件中
view.throttle(5f).clicks { //也支持这样写 view throttle 5f clicks {...}
    //TODO
}

//使用在内部里
view.clicks {
    throttle(5f) {
        //TODO
    }
}

eg : 
//支持infix
button throttle 5f clicks {
    interval(5f) {
        start {
            isEnabled = false
        }
        completed {
            isEnabled = true
        }
        next {
            text = this.toString()
        }
    }
}
//or 
button.clicks {
    throttle(5f) {
        interval(5f) {
            ...
        }
    }
}
```

