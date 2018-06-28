# KotlinUtils

##### shredPreferences

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
doAsync{
    //TODO 
}

doAsync {
    
    //使用主线程
    uiThread {
        
    }
}

T.doAsync {
    //TODO
}
```

##### toast

```kotlin
在Context下使用
//Toast.LENGHT_SHORT
toast("")
//Toast.LENGTH_LONG
toastLong("")
```

#### wait

```kotlin
//等待执行 默认为500ms
wait {
    
}

T.wait {
    
}

wait(millis){
    
}
```

#### eventbus

```kotlin
//订阅
//订阅里只能订阅一种类型，相同类型暂时不支持，如果有相同类型则会覆盖上一个
subscriptions {
    //订阅指定类型
    subscribe(type) just {
        
    }
    //订阅粘性事件
    subscribe(type) sticky {
        
    }
}

eg :
subscriptions {
    subscribe(UserInfo::class) just {
        //TODO
    }
    subscribe(ModifyEvent::class) sticky {
        //TODO
    }
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

