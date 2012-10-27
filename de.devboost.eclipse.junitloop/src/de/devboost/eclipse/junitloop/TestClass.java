package de.devboost.eclipse.junitloop;

public class TestClass {

	private String containingProject;
	private String qualifiedClassName;
	
	public TestClass(String containingProject, String qualifiedClassName) {
		super();
		this.containingProject = containingProject;
		this.qualifiedClassName = qualifiedClassName;
	}
	
	public String getQualifiedClassName() {
		return qualifiedClassName;
	}
	
	public String getContainingProject() {
		return containingProject;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((containingProject == null) ? 0 : containingProject
						.hashCode());
		result = prime
				* result
				+ ((qualifiedClassName == null) ? 0 : qualifiedClassName
						.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestClass other = (TestClass) obj;
		if (containingProject == null) {
			if (other.containingProject != null)
				return false;
		} else if (!containingProject.equals(other.containingProject))
			return false;
		if (qualifiedClassName == null) {
			if (other.qualifiedClassName != null)
				return false;
		} else if (!qualifiedClassName.equals(other.qualifiedClassName))
			return false;
		return true;
	}
}
