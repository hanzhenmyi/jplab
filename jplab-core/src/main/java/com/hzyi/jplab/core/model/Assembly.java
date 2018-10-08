package com.hzyi.jplab.core.model;

import com.hzyi.jplab.core.util.Buildable;
import com.hzyi.jplab.core.viewer.Displayer;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Assembly implements Buildable {

  private String name;
  private final Map<String, Component> components;
  private final Displayer displayer;
  private final AssemblyState initState;

  private Assembly(Builder builder) {
    this.name = builder.name;
    this.components = builder.components;
    this.displayer = builder.displayer;
    this.initState = newAssemblyStateBuilder(builder).build();
  }
  
  /**
   * @returns the name of the {@code Assembly}.
   *
   */
  public String getName() {
    return name;
  }

  /**
   * @returns a {@code Component} based on the name.
   * @throws IllegalArgumentException if the {@code Component} does not exist.
   *
   */
  public Component getComponent(String name) {
    Component component = components.get(name);
    if (component == null) {
      throw new IllegalArgumentException("Component with name " + name + " does not exist.");
    }
    return component;
  }

  /**
   * @returns the collection of all components.
   *
   */
  public Iterable<Component> getComponents() {
    return components.values();
  }

  /**
   * @returns the collection of the names of all components.
   *
   */
  public Iterable<String> getComponentNames() {
    return components.keySet();
  }

  /**
   * @returns the initial {@code AssemblyState} of the {@code Assembly}.
   *
   */
  public AssemblyState getInitialAssemblyState() {
    return this.initState;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  private AssemblyState.Builder newAssemblyStateBuilder(Builder builder) {
    return AssemblyState
        .newBuilder()
        .setAssembly(this)
        .addAll(builder.components.values().stream().map(x -> x.getInitialComponentState()).collect(Collectors.toList()));
  }

  /** Builder of {@code Assembly}. */
  public static class Builder implements com.hzyi.jplab.core.util.Builder<Builder> {
    
    private String name;
    private final Map<String, Component> components = new HashMap<>();
    private Displayer displayer;

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder add(Component component) {
      if (components.containsKey(component.getName())) {
        throw new IllegalArgumentException("Component name already exists.");
      }
      this.components.put(component.getName(), component);
      return this;
    }

    public Builder addAll(Collection<Component> components) {
      for (Component component : components) {
        add(component);
      }
      return this;
    }

    public Builder addAll(Displayer displayer) {
      this.displayer = displayer;
      return this;
    }

    @Override
    public Assembly build() {
      return new Assembly(this);
    }
  }
}