package com.astamuse.asta4d.web.dispatch;

import java.util.ArrayList;
import java.util.List;

import com.astamuse.asta4d.interceptor.base.ExceptionHandler;
import com.astamuse.asta4d.interceptor.base.Executor;
import com.astamuse.asta4d.interceptor.base.GenericInterceptor;
import com.astamuse.asta4d.interceptor.base.InterceptorUtil;
import com.astamuse.asta4d.web.dispatch.interceptor.RequestHandlerInterceptor;
import com.astamuse.asta4d.web.dispatch.interceptor.RequestHandlerResultHolder;
import com.astamuse.asta4d.web.dispatch.mapping.UrlMappingRule;
import com.astamuse.asta4d.web.dispatch.request.RequestHandler;
import com.astamuse.asta4d.web.dispatch.request.ResultTransformer;
import com.astamuse.asta4d.web.dispatch.request.ResultTransformerUtil;
import com.astamuse.asta4d.web.dispatch.response.provider.ContentProvider;
import com.astamuse.asta4d.web.util.AnnotationMethodHelper;

public class DefaultRequestHandlerInvoker implements RequestHandlerInvoker {

    /* (non-Javadoc)
     * @see com.astamuse.asta4d.web.dispatch.RequestHandlerInvoker#invoke(com.astamuse.asta4d.web.dispatch.mapping.UrlMappingRule)
     */
    @Override
    public Object invoke(UrlMappingRule rule) throws Exception {
        RequestHandlerInvokeExecutor executor = new RequestHandlerInvokeExecutor(rule.getHandlerList(), rule.getResultTransformerList());
        RequestHandlerResultHolder holder = new RequestHandlerResultHolder();
        InterceptorUtil.executeWithInterceptors(holder, buildInterceptorList(rule), executor);
        return holder.getResult();
    }

    /*
    private static WebPageView getForwardPageView(Map<Class<? extends ForwardDescriptor>, String> forwardDescriptors,
            ForwardDescriptor forwardDescriptor) {
        String path = forwardDescriptors.get(forwardDescriptor.getClass());
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        return new WebPageView(path);
    }
    */

    private List<RequestHandlerInterceptorWrapper> buildInterceptorList(UrlMappingRule rule) {
        List<RequestHandlerInterceptorWrapper> list = new ArrayList<>();
        for (RequestHandlerInterceptor interceptor : rule.getInterceptorList()) {
            list.add(new RequestHandlerInterceptorWrapper(rule, interceptor));
        }
        return list;
    }

    private static class RequestHandlerInterceptorWrapper implements GenericInterceptor<RequestHandlerResultHolder> {

        private final UrlMappingRule rule;
        private final RequestHandlerInterceptor interceptor;

        public RequestHandlerInterceptorWrapper(UrlMappingRule rule, RequestHandlerInterceptor interceptor) {
            this.rule = rule;
            this.interceptor = interceptor;
        }

        @Override
        public boolean beforeProcess(RequestHandlerResultHolder holder) throws Exception {
            interceptor.preHandle(rule, holder);
            return holder.getResult() == null;
        }

        @Override
        public void afterProcess(RequestHandlerResultHolder holder, ExceptionHandler exceptionHandler) {
            interceptor.postHandle(rule, holder, exceptionHandler);
        }
    }

    private static class RequestHandlerInvokeExecutor implements Executor<RequestHandlerResultHolder> {

        private final List<Object> requestHandlerList;

        private final List<ResultTransformer> resultTransformerList;

        public RequestHandlerInvokeExecutor(List<Object> requestHandlerList, List<ResultTransformer> resultTransformerList) {
            this.requestHandlerList = requestHandlerList;
            this.resultTransformerList = resultTransformerList;
        }

        @Override
        public void execute(RequestHandlerResultHolder holder) throws Exception {
            List<ContentProvider<?>> cpList = new ArrayList<>();
            holder.setResult(cpList);
            Object result;
            ContentProvider<?> cp;
            for (Object handler : requestHandlerList) {
                result = AnnotationMethodHelper.invokeMethodForAnnotation(handler, RequestHandler.class);
                if (result != null) {
                    cp = ResultTransformerUtil.transform(result, resultTransformerList);
                    cpList.add(cp);
                    if (!cp.isContinuable()) {
                        break;
                    }
                }// result != null
            }// for
            if (cpList.isEmpty()) {
                cpList.add(ResultTransformerUtil.transform(null, resultTransformerList));
            }

        }

    }

}
