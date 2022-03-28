package me.fycz.maple;


/**
 * @author fengyue
 * @date 2022/3/28 15:08
 */
public abstract class MethodHook {
    /**
     * Called before the invocation of the method.
     *
     * <p>You can use {@link MapleBridge.MethodHookParam#setResult} and {@link MapleBridge.MethodHookParam#setThrowable}
     * to prevent the original method from being called.
     *
     * <p>Note that implementations shouldn't call {@code super(param)}, it's not necessary.
     *
     * @param param Information about the method call.
     * @throws Throwable Everything the callback throws is caught and logged.
     */
    protected void beforeHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
    }

    /**
     * Called after the invocation of the method.
     *
     * <p>You can use {@link MapleBridge.MethodHookParam#setResult} and {@link MapleBridge.MethodHookParam#setThrowable}
     * to modify the return value of the original method.
     *
     * <p>Note that implementations shouldn't call {@code super(param)}, it's not necessary.
     *
     * @param param Information about the method call.
     * @throws Throwable Everything the callback throws is caught and logged.
     */
    protected void afterHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
    }
}
