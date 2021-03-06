package me.fycz.maple;


/**
 * A special case of {@link MethodHook} which completely replaces the original method.
 */
public abstract class MethodReplacement extends MethodHook {

    /**
     * @hide
     */
    @Override
    protected final void beforeHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
        try {
            Object result = replaceHookedMethod(param);
            param.setResult(result);
        } catch (Throwable t) {
            MapleUtils.log(t);
            param.setThrowable(t);
        }
    }

    /**
     * @hide
     */
    @Override
    @SuppressWarnings("EmptyMethod")
    protected final void afterHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
    }

    /**
     * Shortcut for replacing a method completely. Whatever is returned/thrown here is taken
     * instead of the result of the original method (which will not be called).
     *
     * <p>Note that implementations shouldn't call {@code super(param)}, it's not necessary.
     *
     * @param param Information about the method call.
     * @throws Throwable Anything that is thrown by the callback will be passed on to the original caller.
     */
    @SuppressWarnings("UnusedParameters")
    protected abstract Object replaceHookedMethod(MapleBridge.MethodHookParam param) throws Throwable;

    /**
     * Predefined callback that skips the method without replacements.
     */
    public static final MethodReplacement DO_NOTHING = new MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
            return null;
        }
    };

    /**
     * Creates a callback which always returns a specific value.
     *
     * @param result The value that should be returned to callers of the hooked method.
     */
    public static MethodReplacement returnConstant(final Object result) {
        return new MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
                return result;
            }
        };
    }

}
