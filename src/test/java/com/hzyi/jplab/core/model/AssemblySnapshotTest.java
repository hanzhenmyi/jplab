package com.hzyi.jplab.core.model;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableTable;
import com.hzyi.jplab.core.application.Application;
import com.hzyi.jplab.core.model.kinematic.MassPoint;
import com.hzyi.jplab.core.model.kinematic.SpringModel;
import com.hzyi.jplab.core.model.kinematic.StaticModel;
import com.hzyi.jplab.core.util.DictionaryMatrix;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AssemblySnapshotTest {

  private MassPoint massPoint;
  private SpringModel springModel;
  private StaticModel staticModel;
  private AssemblySnapshot snapshot;

  @Before
  public void setUp() {
    massPoint = MassPoint.newBuilder().name("mass").mass(1.0).build();
    staticModel = StaticModel.newBuilder().name("wall").x(2.0).build();
    springModel =
        SpringModel.newBuilder()
            .name("spring")
            .stiffness(1.0)
            .originalLength(1.0)
            .connectingModelA(massPoint)
            .connectingModelB(staticModel)
            .build();
    snapshot =
        AssemblySnapshot.empty()
            .withKinematicModel(massPoint)
            .withKinematicModel(staticModel)
            .withKinematicModel(springModel);
    snapshot.makeImmutable();
  }

  @Test
  public void testPackUnpack() {
    assertThat(massPoint.merge(massPoint.pack())).isEqualTo(massPoint);
    assertThat(springModel.merge(springModel.pack())).isEqualTo(springModel);
    assertThat(staticModel.merge(staticModel.pack())).isEqualTo(staticModel);
  }

  @Test
  public void testPackUpdateUnpack() {
    Map<String, Object> massPointMap = massPoint.pack();
    massPointMap.put("x", (Double) 10.0);
    MassPoint updatedMassPoint = massPoint.merge(massPointMap);
    assertThat(updatedMassPoint).isEqualTo(massPoint.toBuilder().x(10.0).build());
    Map<String, Object> springMap = springModel.pack();
    springMap.put("connecting_model_a", updatedMassPoint);
    assertThat(springModel.merge(springMap))
        .isEqualTo(springModel.toBuilder().connectingModelA(updatedMassPoint).build());
  }

  @Test
  public void testUnpack() {
    snapshot =
        snapshot.merge(
            ImmutableTable.<String, String, Double>builder()
                .put("mass", "x", -10.0)
                .put("mass", "ax", 5.0)
                .build());
    MassPoint newMassPoint = massPoint.toBuilder().x(-10.0).ax(5.0).build();
    SpringModel newSpring = springModel.toBuilder().connectingModelA(newMassPoint).build();
    assertThat((MassPoint) snapshot.getKinematicModel("mass")).isEqualTo(newMassPoint);
    assertThat((StaticModel) snapshot.getKinematicModel("wall")).isEqualTo(staticModel);
    assertThat((SpringModel) snapshot.getKinematicModel("spring")).isEqualTo(newSpring);
  }

  @Test
  public void testToMatrix() {
    Application.init(null, Assembly.getInstance(), null, null, null, 1.0);
    DictionaryMatrix matrix = snapshot.getCodependentMatrix(1.0);
    assertThat(matrix.getRow("mass.ax"))
        .containsExactly(
            "mass.x",
            0.0,
            "mass.y",
            0.0,
            "mass.vx",
            0.0,
            "mass.vy",
            0.0,
            "mass.ax",
            -1.0,
            "mass.ay",
            0.0,
            "_constant",
            1.0);
    assertThat(matrix.getRow("mass.ay"))
        .containsExactly(
            "mass.x",
            0.0,
            "mass.y",
            0.0,
            "mass.vx",
            0.0,
            "mass.vy",
            0.0,
            "mass.ax",
            0.0,
            "mass.ay",
            -1.0,
            "_constant",
            0.0);
    Map<String, Double> answer = matrix.solve();
    assertThat(answer)
        .containsExactly(
            "mass.x", 0.0, "mass.y", 0.0, "mass.vx", 0.0, "mass.vy", 0.0, "mass.ax", 1.0, "mass.ay",
            -0.0);
    Application.reset();
  }
}
