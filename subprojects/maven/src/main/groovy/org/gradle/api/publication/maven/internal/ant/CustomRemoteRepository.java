package org.gradle.api.publication.maven.internal.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.eclipse.aether.internal.ant.AntRepoSys;
import org.eclipse.aether.internal.ant.types.Proxy;
import org.eclipse.aether.internal.ant.types.RemoteRepository;

class CustomRemoteRepository extends RemoteRepository {

    private final List<Proxy> proxies = new ArrayList<Proxy>();

    @Override
    public void setProject(Project project) {
        super.setProject(project);

        AntRepoSys repoSys = AntRepoSys.getInstance(project);
        for (Proxy proxy : proxies) {
            repoSys.addProxy(proxy);
        }
    }

    public void addProxy(Proxy proxy) {
        proxies.add(proxy);
    }

}
