/*
 *
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.netflix.client.netty.http;

import io.netty.bootstrap.Bootstrap;
import io.reactivex.netty.protocol.http.ObservableHttpResponse;
import io.reactivex.netty.protocol.text.sse.SSEEvent;
import rx.Observable;

import com.netflix.client.LoadBalancerErrorHandler;
import com.netflix.client.ClientObservableProvider;
import com.netflix.client.LoadBalancerObservables;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerStats;
import com.netflix.serialization.HttpSerializationContext;
import com.netflix.serialization.SerializationFactory;
import com.netflix.serialization.TypeDef;

public class NettyHttpLoadBalancingClient extends NettyHttpClient {

    private LoadBalancerObservables<HttpRequest, HttpResponse> lbObservables;
    private final NettyHttpClient delegate;
    
    public NettyHttpLoadBalancingClient() {
        this(DefaultClientConfigImpl.getClientConfigWithDefaultValues());
    }
    
    public NettyHttpLoadBalancingClient(IClientConfig config) {
        delegate = new NettyHttpClient(config);
        lbObservables = new LoadBalancerObservables<HttpRequest, HttpResponse>(config);
        lbObservables.setErrorHandler(new NettyHttpLoadBalancerErrorHandler());
    }
    
    public NettyHttpLoadBalancingClient(IClientConfig config, LoadBalancerErrorHandler<HttpRequest, HttpResponse> errorHandler, 
            SerializationFactory<HttpSerializationContext> serializationFactory, Bootstrap bootStrap) {
        delegate = new NettyHttpClient(config, serializationFactory, bootStrap);
        this.lbObservables = new LoadBalancerObservables<HttpRequest, HttpResponse>(config);
        lbObservables.setErrorHandler(errorHandler);
    }
    
    @Override
    public IClientConfig getConfig() {
        return delegate.getConfig();
    }

    @Override
    public SerializationFactory<HttpSerializationContext> getSerializationFactory() {
        return delegate.getSerializationFactory();
    }

    @Override
    public <T> Observable<ServerSentEvent<T>> createServerSentEventEntityObservable(
            final HttpRequest request, final TypeDef<T> typeDef, final IClientConfig requestConfig) {
        return lbObservables.retryWithLoadBalancer(request, new ClientObservableProvider<ServerSentEvent<T>, HttpRequest>() {

            @Override
            public Observable<ServerSentEvent<T>> getObservableForEndpoint(
                    HttpRequest request) {
                return delegate.createServerSentEventEntityObservable(request, typeDef, requestConfig);
            }
            
        }, requestConfig);
    }

    @Override
    public Observable<ObservableHttpResponse<SSEEvent>> createServerSentEventObservable(
            HttpRequest request, final IClientConfig requestConfig) {
        return lbObservables.retryWithLoadBalancer(request, new ClientObservableProvider<ObservableHttpResponse<SSEEvent>, HttpRequest>() {

            @Override
            public Observable<ObservableHttpResponse<SSEEvent>> getObservableForEndpoint(HttpRequest _request) {
                return delegate.createServerSentEventObservable(_request, requestConfig);
            }
        }, requestConfig);
    }

    @Override
    public Observable<HttpResponse> createFullHttpResponseObservable(
            HttpRequest request, final IClientConfig requestConfig) {
        return lbObservables.retryWithLoadBalancer(request, new ClientObservableProvider<HttpResponse, HttpRequest>() {

            @Override
            public Observable<HttpResponse> getObservableForEndpoint(
                    HttpRequest request) {
                return delegate.createFullHttpResponseObservable(request, requestConfig);
            }
            
        }, requestConfig);
    }

    @Override
    public <T> Observable<T> createEntityObservable(HttpRequest request,
            final TypeDef<T> typeDef, final IClientConfig requestConfig) {
        return lbObservables.retryWithLoadBalancer(request, new ClientObservableProvider<T, HttpRequest>() {

            @Override
            public Observable<T> getObservableForEndpoint(HttpRequest _request) {
                return delegate.createEntityObservable(_request, typeDef, requestConfig);
            }
        }, requestConfig);
   }

    public void setLoadBalancer(ILoadBalancer lb) {
        lbObservables.setLoadBalancer(lb);
    }
    
    public ILoadBalancer getLoadBalancer() {
        return lbObservables.getLoadBalancer();
    }
    
    public int getMaxAutoRetriesNextServer() {
        return lbObservables.getMaxAutoRetriesNextServer();
    }

    public void setMaxAutoRetriesNextServer(int maxAutoRetriesNextServer) {
        lbObservables.setMaxAutoRetriesNextServer(maxAutoRetriesNextServer);
    }

    public int getMaxAutoRetries() {
        return lbObservables.getMaxAutoRetries();
    }

    public void setMaxAutoRetries(int maxAutoRetries) {
        lbObservables.setMaxAutoRetries(maxAutoRetries);
    }

    public ServerStats getServerStats(Server server) {
        return lbObservables.getServerStats(server);
    }

}